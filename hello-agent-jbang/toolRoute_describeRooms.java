//DEPS org.apache.camel:camel-bom:4.17.0-SNAPSHOT@pom
//DEPS org.apache.camel:camel-core
//DEPS org.apache.camel:camel-langchain4j-agent
//DEPS dev.langchain4j:langchain4j-ollama:1.9.1

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.langchain4j.agent.api.Agent;
import org.apache.camel.component.langchain4j.agent.api.AgentConfiguration;
import org.apache.camel.component.langchain4j.agent.api.AgentWithoutMemory;
import org.apache.camel.component.langchain4j.agent.api.Headers;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.ollama.OllamaChatModel;

public class toolRoute_describeRooms extends RouteBuilder {

    @Override
    public void configure() throws Exception {

        ChatModel ollamaModel = OllamaChatModel.builder()
                .baseUrl("http://localhost:11434")
                .temperature(0.0)
                .logRequests(false)
                .logResponses(false)
                .modelName("granite4:1b")
                .build();

        // Create agent configuration
        AgentConfiguration configuration = new AgentConfiguration()
                .withChatModel(ollamaModel);

        // Create the agent
        Agent simpleAgent = new AgentWithoutMemory(configuration);

        // Register the agent in the Camel context
        getContext().getRegistry().bind("simpleAgent", simpleAgent);

        // @formatter:off
        from("direct:adventure")
            .to("log:adventure")
            .to("langchain4j-agent:test?agent=#simpleAgent&tags=rooms")
            .to("log:adventure");

        from("langchain4j-tools:roomDB?tags=rooms&description=Query room database&parameter.name=string")
            .setBody(constant(
                    "{\"name\": \"entrance\", \"features\": [\"a crumbled gate leading north, flanked by statues\", \"a decayed bridge over a moat filled with muck\"]}"));

        from("timer:runOnce?repeatCount=1&delay=3000")
            .setHeader(Headers.SYSTEM_MESSAGE).constant("""
                You are a text-based adventure set in a ruined castle.
                In the style of a fantasy writer, lookup the features of a room by its name and return a lurid description.
                    """)
            .setBody().constant("I enter the room named 'entrance'")
            .to("direct:adventure");
        // @formatter:on
    }
}
