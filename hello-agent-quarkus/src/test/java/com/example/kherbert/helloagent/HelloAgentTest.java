package com.example.kherbert.helloagent;

import io.quarkus.test.junit.QuarkusTest;

import org.apache.camel.component.langchain4j.agent.api.AiAgentBody;
import org.apache.camel.quarkus.test.CamelQuarkusTestSupport;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;


@QuarkusTest
public class HelloAgentTest  extends CamelQuarkusTestSupport {

    @Test
    @Disabled
    public void hello() {
        final String PROMPT = "Hello Mr Agent.";

        template.sendBody("direct:chat", PROMPT);
    }

    @Test
    public void describeRooms() {
        var systemPrompt = """
        You are a text-based adventure set in a ruined castle. Use the tool to lookup the features of a room by its name and return a vivid description.
        """;
        var userPrompt = "I enter the room named 'entrance'";

        template.sendBody("direct:chat", new AiAgentBody<>()
                .withSystemMessage(systemPrompt)
                .withUserMessage(userPrompt));
    }
}