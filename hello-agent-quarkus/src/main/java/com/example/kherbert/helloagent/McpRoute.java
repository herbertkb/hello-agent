/* This feature not available in Camel-Quarkus 3.27 / Camel 4.14 
package com.example.kherbert.helloagent;

import java.util.Arrays;
import java.util.List;

import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.langchain4j.agent.api.Agent;
import org.apache.camel.component.langchain4j.agent.api.AgentConfiguration;
import org.apache.camel.component.langchain4j.agent.api.AgentWithoutMemory;

import dev.langchain4j.mcp.client.DefaultMcpClient;
import dev.langchain4j.mcp.client.McpClient;
import dev.langchain4j.mcp.client.transport.McpTransport;
import dev.langchain4j.mcp.client.transport.stdio.StdioMcpTransport;

import dev.langchain4j.model.openai.OpenAiChatModel;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class McpRoute extends RouteBuilder {

    @Inject
    CamelContext context;

    @Override
    public void configure() throws Exception {
        // demo model setup:
        // https://docs.langchain4j.dev/integrations/language-models/open-ai
        OpenAiChatModel model = OpenAiChatModel.builder()
                .baseUrl("http://langchain4j.dev/demo/openai/v1")
                .apiKey("demo")
                .modelName("gpt-4o-mini")
                .build();

        // Create MCP client for filesystem access
        McpTransport transport = new StdioMcpTransport.Builder()
                .command(Arrays.asList("npx", "-y", "@modelcontextprotocol/server-filesystem", "/tmp"))
                .logEvents(true)
                .build();

        McpClient mcpClient = new DefaultMcpClient.Builder()
                .transport(transport)
                .build();

        // Create agent with MCP tools
        AgentConfiguration config = new AgentConfiguration()
                .withChatModel(model)
                .withMcpClient(mcpClient);

        // Create the agent
        Agent mcpAgent = new AgentWithoutMemory(config);

        // Register the agent in the Camel context
        context.getRegistry().bind("mcpAgent", mcpAgent);
    }

}
*/