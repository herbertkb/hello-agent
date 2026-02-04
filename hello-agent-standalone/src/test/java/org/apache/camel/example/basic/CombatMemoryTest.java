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

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.langchain4j.agent.api.Agent;
import org.apache.camel.component.langchain4j.agent.api.AgentConfiguration;
import org.apache.camel.component.langchain4j.agent.api.AgentWithMemory;
import org.apache.camel.component.langchain4j.agent.api.AiAgentBody;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.spi.Registry;
import org.apache.camel.test.junit5.CamelTestSupport;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import dev.langchain4j.store.memory.chat.InMemoryChatMemoryStore;
import java.util.List;
import java.util.Map;
import static org.apache.camel.component.langchain4j.agent.api.Headers.SYSTEM_MESSAGE;
import static org.apache.camel.component.langchain4j.agent.api.Headers.MEMORY_ID;

/**
 * A unit test checking that Camel can be launched in standalone mode.
 */
class CombatMemoryTest extends CamelTestSupport {
    private final Logger LOG = LoggerFactory.getLogger(getClass());

    @Test
    @Disabled
    public void combat() throws InterruptedException {
        MockEndpoint mock = getMockEndpoint("mock:output");
        mock.expectedMessageCount(2);

        var systemPrompt = """
                You are a text-based adventure set in a ruined castle.
                There is a skeleton advancing towards me.
                In the style of a fantasy writer, describe the actions and result of combat using tools to calculate the damage.
                Describe the impact of the mace on the skeleton's bones.
                My weapon deals some number of damage points.
                Those damage points are subtracted from the hit points of an enemy.
                The skeleton has 10 hit points.
                The skeleton will crumple to dust if it has less than 1 hit point.
                The skeleton is still standing if it has atleast 1 hit point.
                """;
        var userPrompt = "I swing my mace at the skeleton";
        var memoryId = "session_02";
        AiAgentBody agentBody = new AiAgentBody(userPrompt, systemPrompt, memoryId);

        String response1 = template.requestBody("direct:combat", agentBody, String.class);
        LOG.info("Round 1:: {}", response1);

        String response2 = template.requestBodyAndHeaders("direct:combat", 
                "I swing my mace again at the skeleton.", 
                Map.of(SYSTEM_MESSAGE, """
                You are a text-based adventure set in a ruined castle.
                The skeleton is still standing before me.
                In the style of a fantasy writer, describe the actions and result of combat using tools to calculate the damage.
                Describe the impact of the mace on the skeleton's bones.
                My weapon deals some number of damage points.
                Those damage points are subtracted from the hit points of an enemy.
                The skeleton will crumple to dust if it has less than 1 hit point.
                The skeleton is still standing if it has atleast 1 hit point.
                        """,
                    MEMORY_ID, memoryId),
                String.class);
        LOG.info("Round 2:: {}", response2);

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

        ChatMemoryStore store = new InMemoryChatMemoryStore();

        // Example of creating a Chat Memory Provider : Create a message window memory that keeps the last 10 messages
        ChatMemoryProvider chatMemoryProvider = memoryId -> MessageWindowChatMemory.builder()
                .id(memoryId)
                .maxMessages(10)
                .chatMemoryStore(store) // the Chat Memory store is previously created
                .build();

        // Create agent configuration
        AgentConfiguration configuration = new AgentConfiguration()
                .withChatModel(ollamaModel)
                .withChatMemoryProvider(chatMemoryProvider)
                .withCustomTools(List.of(new CombatTool()))
                .withInputGuardrailClasses(List.of())
                .withOutputGuardrailClasses(List.of());

        // Create the agent
        Agent memoryAgent = new AgentWithMemory(configuration);

        // Register the agent in the Camel context
        registry.bind("memoryAgent", memoryAgent);
    }

    @Override
    protected RouteBuilder createRouteBuilder() {
        return new RouteBuilder() {
            @Override
            public void configure() {
                // @formatter:off
                from("direct:combat")
                    .to("log:combat")
                    .to("langchain4j-agent:test?agent=#memoryAgent")
                    .to("log:combat")
                    .to("mock:output");
                // @formatter:on
            }
        };
    }
}
