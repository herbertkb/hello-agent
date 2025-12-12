package org.apache.camel.example.basic;

import org.apache.camel.CamelExecutionException;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.langchain4j.agent.api.Agent;
import org.apache.camel.component.langchain4j.agent.api.AgentConfiguration;
import org.apache.camel.component.langchain4j.agent.api.AgentWithoutMemory;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.spi.Registry;
import org.apache.camel.test.junit5.CamelTestSupport;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.langchain4j.guardrail.InputGuardrailException;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.ollama.OllamaChatModel;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

class GuardrailTest extends CamelTestSupport {
    private final Logger LOG = LoggerFactory.getLogger(getClass());

    @Test
    // @Disabled
    public void rejectedInput() throws InterruptedException {
        MockEndpoint mock = getMockEndpoint("mock:output");
        mock.expectedMessageCount(0);

        final String PROMPT = "Tell me about the Red King.";

        CamelExecutionException ex = assertThrows(CamelExecutionException.class, () ->
            template.sendBody("direct:chat", PROMPT)
        );

        assertTrue( ex.getCause().getClass().equals(InputGuardrailException.class) );

        mock.assertIsSatisfied(10L * 1000L);
    }

    @Override
    protected void bindToRegistry(Registry registry) throws Exception {

        ChatModel ollamaModel = OllamaChatModel.builder()
                .baseUrl("http://localhost:11434")
                .temperature(0.0)
                .logRequests(true)
                .logResponses(true)
                .modelName("granite4:1b")
                .build();

        // Create agent configuration
        AgentConfiguration configuration = new AgentConfiguration()
                .withChatModel(ollamaModel)
                .withInputGuardrailClasses(List.of(DoNotAskAboutTheRedKingInputGuardrail.class))
                .withOutputGuardrailClasses(List.of());

        // Create the agent
        Agent simpleAgent = new AgentWithoutMemory(configuration);

        // Register the agent in the Camel context
        registry.bind("simpleAgent", simpleAgent);
    }

    @Override
    protected RouteBuilder createRouteBuilder() {
        return new RouteBuilder() {
            @Override
            public void configure() {
                // @formatter:off
                from("direct:chat")
                    .to("log:chat")
                    .to("langchain4j-agent:test?agent=#simpleAgent")
                    .to("log:chat")
                    .to("mock:output");
                // @formatter:on
            }
        };
    }
}
