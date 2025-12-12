package org.apache.camel.example.basic;

import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiPredicate;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.langchain4j.agent.api.Agent;
import org.apache.camel.component.langchain4j.agent.api.AgentConfiguration;
import org.apache.camel.component.langchain4j.agent.api.AgentWithoutMemory;
import static org.apache.camel.component.langchain4j.agent.api.Headers.SYSTEM_MESSAGE;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.spi.Registry;
import org.apache.camel.test.junit5.CamelTestSupport;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.mcp.client.DefaultMcpClient;
import dev.langchain4j.mcp.client.McpClient;
import dev.langchain4j.mcp.client.transport.McpTransport;
import dev.langchain4j.mcp.client.transport.stdio.StdioMcpTransport;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.ollama.OllamaChatModel;

public class McpCastleTest extends CamelTestSupport {
    private final Logger LOG = LoggerFactory.getLogger(getClass());

    String castleDir;

    public McpCastleTest() throws URISyntaxException {
        // castleDir =
        // Paths.get(getClass().getClassLoader().getResource("castle").toURI()).toString();
        // castleDir = "/tmp/castle";
        castleDir = "castle";
    }

    @Test
    @Disabled
    public void mcp() throws InterruptedException, URISyntaxException {
        MockEndpoint mock = getMockEndpoint("mock:output");
        mock.expectedMessageCount(1);

        var systemPrompt = "You are allowed to read the " + castleDir + " directory.";
        var userPrompt = "The contents of the file castle/dungeon.txt are the contents of a room in an abandoned castle. Vividly describe this room.";


        // var userPrompts = List.of("I enter the gatehouse.", "I enter the throneroom",
        // "I enter the dungeon");

        // userPrompts.forEach((prompt) ->
        // template.requestBodyAndHeader("direct:castle", prompt,
        // Headers.SYSTEM_MESSAGE, systemPrompt));

        // template.requestBodyAndHeader("direct:castle", """
        // These files are rooms of an abandoned castle.
        // Describe in vivid detail what each room looks like.
        // """, Headers.SYSTEM_MESSAGE, systemPrompt);

        String response = template.requestBodyAndHeader("direct:castle", userPrompt, SYSTEM_MESSAGE, systemPrompt,String.class);
        LOG.info("Response: {}", response);

        // template.requestBody("direct:castle", "The files in " + castleDir
        // + " are rooms of an abandoned castle. Describe in vivid detail what each room
        // looks like.");

        mock.assertIsSatisfied(10L * 1000L);
    }

    @Override
    protected void bindToRegistry(Registry registry) throws Exception {

        // Create MCP client for filesystem access
        McpTransport transport = new StdioMcpTransport.Builder()
                .command(Arrays.asList("npx", "-y", "@modelcontextprotocol/server-filesystem", castleDir))
                .logEvents(true)
                .build();

        McpClient mcpClient = new DefaultMcpClient.Builder()
                .transport(transport)
                .build();

        // Create agent with MCP tools
        ChatModel model = OllamaChatModel.builder()
                .baseUrl("http://localhost:11434")
                .temperature(0.0)
                .logRequests(true)
                .logResponses(true)
                .modelName("granite4:1b")
                .build();
        // Create security filter to only allow read operations
        BiPredicate<McpClient, ToolSpecification> securityFilter = (client, toolSpec) -> {
            String toolName = toolSpec.name().toLowerCase();
            // Only allow read operations for safety
            return toolName.contains("read") || toolName.contains("list") || toolName.contains("get");
        };
        AgentConfiguration config = new AgentConfiguration()
                .withChatModel(model)
                .withMcpClient(mcpClient)
                .withMcpToolProviderFilter(securityFilter);

        // Create the agent
        Agent mcpAgent = new AgentWithoutMemory(config);

        // Register the agent in the Camel context
        registry.bind("mcpAgent", mcpAgent);
    }

    @Override
    protected RouteBuilder createRouteBuilder() {
        return new RouteBuilder() {
            @Override
            public void configure() {
                // @formatter:off
                from("direct:castle")
                    .to("langchain4j-agent:test?agent=#mcpAgent")
                    .to("log:castle")
                    .to("mock:output");
                // @formatter:on
            }
        };
    }
}
