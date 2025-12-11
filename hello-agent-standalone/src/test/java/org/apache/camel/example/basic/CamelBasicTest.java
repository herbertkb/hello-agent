/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.camel.example.basic;

import org.apache.camel.CamelContext;
import org.apache.camel.RoutesBuilder;
import org.apache.camel.builder.NotifyBuilder;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.langchain4j.agent.api.Agent;
import org.apache.camel.component.langchain4j.agent.api.AgentConfiguration;
import org.apache.camel.component.langchain4j.agent.api.AgentWithoutMemory;
import org.apache.camel.component.langchain4j.agent.api.Headers;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.spi.Registry;
import org.apache.camel.test.junit5.CamelTestSupport;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.langchain4j.mcp.client.DefaultMcpClient;
import dev.langchain4j.mcp.client.McpClient;
import dev.langchain4j.mcp.client.transport.stdio.StdioMcpTransport;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.ollama.OllamaChatModel;

import dev.langchain4j.mcp.client.DefaultMcpClient;
import dev.langchain4j.mcp.client.McpClient;
import dev.langchain4j.mcp.client.transport.McpTransport;
import dev.langchain4j.mcp.client.transport.stdio.StdioMcpTransport;

import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.apache.camel.example.basic.CamelBasic.createBasicRoute;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A unit test checking that Camel can be launched in standalone mode.
 */
class CamelBasicTest extends CamelTestSupport {
    private final Logger LOG = LoggerFactory.getLogger(getClass());

    @Test
    @Disabled
    public void hello() throws InterruptedException {
        final String PROMPT = "Hello Mr Agent.";

        MockEndpoint mock = getMockEndpoint("mock:output");
        mock.expectedMessageCount(1);

        LOG.info("Prompt: {}", PROMPT);
        String response = template.requestBody("direct:chat", PROMPT, String.class);
        LOG.info("Reponse: {}", response);

        mock.assertIsSatisfied(10L * 1000L);
    }

    @Test
    @Disabled
    public void describeRooms() throws InterruptedException {
        MockEndpoint mock = getMockEndpoint("mock:output");
        mock.expectedMessageCount(1);

        var systemPrompt = """
                You are a text-based adventure set in a ruined castle.
                In the style of a fantasy writer, lookup the features of a room by its name and return a lurid description.
                """;
        var userPrompt = "I enter the room named 'entrance'";

        String response = template.requestBodyAndHeader("direct:adventure", userPrompt, Headers.SYSTEM_MESSAGE,
                systemPrompt, String.class);
        LOG.info("Reponse: {}", response);

        mock.assertIsSatisfied(10L * 1000L);
    }

    @Test
    @Disabled
    public void combat() throws InterruptedException {
        MockEndpoint mock = getMockEndpoint("mock:output");
        mock.expectedMessageCount(1);

        var systemPrompt = """
                You are a text-based adventure set in a ruined castle.
                There is a skeleton advancing towards me.
                In the style of a fantasy writer, describe the actions and result of combat using tools to calculate the damage.
                Describe the impact of the mace on the skeleton's bones.
                My weapon deals some number of damage points.
                Those damage points are subtracted from the hit points of an enemy.
                The skeleton has 3 hit points.
                The skeleton will crumple to dust if it has less than 1 hit point.
                The skeleton is still standing if it has atleast 1 hit point.
                """;
        var userPrompt = "I swing my mace at the skeleton";

        template.requestBodyAndHeader("direct:adventure", userPrompt, Headers.SYSTEM_MESSAGE, systemPrompt);

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
                .withCustomTools(List.of(new CombatTool()))
                .withInputGuardrailClasses(List.of())
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
                    .to("langchain4j-agent:test?agent=#simpleAgent")
                    .to("log:chat")
                    .to("mock:output");

                from("direct:adventure")
                    .to("log:adventure")
                    .to("langchain4j-agent:test?agent=#simpleAgent&tags=rooms,combat")
                    .to("log:adventure")
                    .to("mock:output");
                    
                from("langchain4j-tools:roomDB?tags=rooms&description=Query room database&parameter.name=string")
                    .setBody(constant(
                            "{\"name\": \"entrance\", \"features\": [\"a crumbled gate leading north, flanked by statues\", \"a decayed bridge over a moat filled with muck\"]}"));
                
                from("direct:castle")
                    .to("langchain4j-agent:test?agent=#mcpAgent")
                    .to("log:castle")
                    .to("mock:output");
                // @formatter:on
            }
        };
    }
}
