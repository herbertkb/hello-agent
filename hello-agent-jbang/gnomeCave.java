//DEPS org.apache.camel:camel-bom:4.17.0@pom
//DEPS org.apache.camel:camel-core
//DEPS org.apache.camel:camel-langchain4j-agent
//DEPS dev.langchain4j:langchain4j-ollama:1.9.1

import org.apache.camel.BindToRegistry;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.langchain4j.agent.api.Agent;
import org.apache.camel.component.langchain4j.agent.api.AgentConfiguration;
import org.apache.camel.component.langchain4j.agent.api.AgentWithoutMemory;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.ollama.OllamaChatModel;

public class gnomeCave extends RouteBuilder {

    @BindToRegistry("agent")
    Agent configureAgent() {

        // TODO: externalize this configuration
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
        Agent agent = new AgentWithoutMemory(configuration);
        return agent;
    }

    @Override
    public void configure() throws Exception {

        // @formatter:off
        from("direct:gnomeCave")
            .routeId("gnomeCave")
            .to("langchain4j-agent:test?agent=#agent");

        from("stream:in?promptMessage=>")
            .to("direct:gnomeCave")
            .to("stream:out");
        // @formatter:on
    }
}
