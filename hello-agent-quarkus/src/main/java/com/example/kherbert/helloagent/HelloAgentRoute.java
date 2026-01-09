package com.example.kherbert.helloagent;

import java.util.List;

import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.langchain4j.agent.api.Agent;
import org.apache.camel.component.langchain4j.agent.api.AgentConfiguration;
import org.apache.camel.component.langchain4j.agent.api.AgentWithoutMemory;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
// import dev.langchain4j.model.openai.OpenAiChatModel;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class HelloAgentRoute extends RouteBuilder {

    @Inject
    CamelContext context;

    @PostConstruct
    void setup() {
        // demo model setup:
        // https://docs.langchain4j.dev/integrations/language-models/open-ai
        // OpenAiChatModel openAiModel = OpenAiChatModel.builder()
        //         .baseUrl("http://langchain4j.dev/demo/openai/v1")
        //         .apiKey("demo")
        //         .modelName("gpt-4o-mini")
        //         .build();

        ChatModel ollamaModel = OllamaChatModel.builder()
                .baseUrl("http://localhost:11434")
                .temperature(0.0)
                .logRequests(true)
                .logResponses(true)
                .modelName("granite4:350m-h")
                .build();

        // Create agent configuration
        AgentConfiguration configuration = new AgentConfiguration()
                .withChatModel(ollamaModel)
                .withInputGuardrailClasses(List.of())
                .withOutputGuardrailClasses(List.of());

        // Create the agent
        Agent simpleAgent = new AgentWithoutMemory(configuration);

        // Register the agent in the Camel context
        context.getRegistry().bind("simpleAgent", simpleAgent);
    }

    @Override
    public void configure() throws Exception {
        // @formatter:off
        from("direct:chat")
            .to("langchain4j-agent:test?agent=#simpleAgent")
            .to("log:chat");

        from("direct:adventure")
            .to("log:adventure")
            .to("langchain4j-agent:test?agent=#simpleAgent&tags=rooms")
            .to("log:adventure");

            
        from("langchain4j-tools:roomDB?tags=rooms&description=Query room database&parameter.name=string")
            .setBody(constant(
                    "{\"name\": \"entrance\", \"features\": [\"a crumbled gate leading north, flanked by statues\", \"a decayed bridge over a moat filled with muck\"]}"));
        // @formatter:on
    }

}
