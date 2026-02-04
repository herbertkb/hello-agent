//DEPS org.apache.camel:camel-bom:4.17.0-SNAPSHOT@pom
//DEPS org.apache.camel:camel-core
//DEPS org.apache.camel:camel-langchain4j-agent
//DEPS dev.langchain4j:langchain4j-ollama:1.9.1

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.langchain4j.agent.api.Agent;
import org.apache.camel.component.langchain4j.agent.api.AgentConfiguration;
import org.apache.camel.component.langchain4j.agent.api.AgentWithoutMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.ollama.OllamaChatModel;

public class simplePrompt_hello extends RouteBuilder {

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
        from("direct:chat")
            .routeId("chat")
            .to("langchain4j-agent:test?agent=#simpleAgent")
            .to("log:chat");

        from("timer:runOnce?repeatCount=1&delay=3000")
            .routeId("send one message")
            .setBody().constant("Hello and well met, stranger.")
            .to("direct:chat");
        // @formatter:on
    }
}
