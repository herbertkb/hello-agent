package com.example.kherbert.helloagent;

import io.quarkus.test.junit.QuarkusTest;

import org.apache.camel.component.langchain4j.agent.Headers;
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
        // var systemPrompt = """
        // You are a text-based adventure set in a ruined castle. Use the tool to lookup the features of a room by its name and return a vivid description.
        // """;
        var systemPrompt = """
        You are a text-based adventure set in a ruined castle. 
        In the style of a fantasy writer, lookup the features of a room by its name and return a lurid description.
        """;
        var userPrompt = "I enter the room named 'entrance'";

        template.requestBodyAndHeader("direct:adventure", userPrompt, Headers.SYSTEM_MESSAGE,  systemPrompt);
    }

    @Test
    @Disabled
    public void combat() {
        // var systemPrompt = """
        // You are a text-based adventure set in a ruined castle. Use the tool to lookup the features of a room by its name and return a vivid description.
        // """;
        var systemPrompt = """
        You are a text-based adventure set in a ruined castle.
        There is a skeleton advancing towards me.
        In the style of a fantasy writer, describe the actions and result of combat using tools to calculate the damage.
        The skeleton has 5 hit points. If it has 0 hit points, it crumples to dust.
        """;
        var userPrompt = "I swing my mace at the skeleton";

        template.requestBodyAndHeader("direct:adventure", userPrompt, Headers.SYSTEM_MESSAGE,  systemPrompt);
    }
}