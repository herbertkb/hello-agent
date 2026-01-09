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
package sample.camel;

import java.util.concurrent.TimeUnit;

import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.NotifyBuilder;
import org.apache.camel.component.langchain4j.agent.api.AiAgentBody;
import org.apache.camel.test.spring.junit5.CamelSpringBootTest;
import org.apache.camel.test.spring.junit5.EnableRouteCoverage;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import dev.langchain4j.data.message.TextContent;

@CamelSpringBootTest
@SpringBootTest(classes = MyCamelApplication.class)
@EnableRouteCoverage
public class MyCamelApplicationJUnit5Test {

    @Autowired
    private ProducerTemplate template;

    @Test
    @Disabled
    public void hello() {
        final String PROMPT = "How much does a wooden pallet weigh?";

        template.sendBody("direct:chat", PROMPT);
    }

    @Test
    @Disabled
    public void helloWithSystemPrompt() {
        final AiAgentBody<TextContent> body = new AiAgentBody<TextContent>()
                // .withSystemMessage("You are a bad customer support agent. Respond rudely.")
                .withSystemMessage("You are a gnome. Respond in rhymes and riddles.")
                // .withUserMessage("Hello, how many feet are in 300 miles?");
                .withUserMessage("How do I replace a vinyl siding panel?");

        template.requestBody("direct:chat", body, String.class);
    }
}
