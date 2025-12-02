package com.example.kherbert.helloagent;

import io.quarkus.test.junit.QuarkusTest;

import org.apache.camel.quarkus.test.CamelQuarkusTestSupport;
import org.junit.jupiter.api.Test;


@QuarkusTest
public class HelloAgentTest  extends CamelQuarkusTestSupport {

    @Test
    public void hello() {
        final String PROMPT = "Hello Mr Agent.";

        template.sendBody("direct:chat", PROMPT);
    }
}