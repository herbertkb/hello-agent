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
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.spi.Registry;
import org.apache.camel.test.junit5.CamelTestSupport;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.ollama.OllamaChatModel;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.apache.camel.example.basic.CamelBasic.createBasicRoute;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A unit test checking that Camel can be launched in standalone mode.
 */
class CamelBasicTest extends CamelTestSupport {

    @Test
    public void hello() {
        final String PROMPT = "Hello Mr Agent.";
        template.sendBody("direct:chat", PROMPT);
    }

    // @Override
    // protected RoutesBuilder createRouteBuilder() {
    // return createBasicRoute(new DefaultCamelContext());
    // }

    @Override
    protected void bindToRegistry(Registry registry) throws Exception {

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
        };
    }
}
