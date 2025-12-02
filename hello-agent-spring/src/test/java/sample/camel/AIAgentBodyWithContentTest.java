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
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import dev.langchain4j.data.message.TextContent;

@CamelSpringBootTest
@SpringBootTest(classes = MyCamelApplication.class)
@EnableRouteCoverage
public class AIAgentBodyWithContentTest {

    @Autowired
    private ProducerTemplate template;

    final String bigTextBlock = """
            Call me Ishmael. Some years ago—never mind how long precisely—having
            little or no money in my purse, and nothing particular to interest me
            on shore, I thought I would sail about a little and see the watery part
            of the world. It is a way I have of driving off the spleen and
            regulating the circulation. Whenever I find myself growing grim about
            the mouth; whenever it is a damp, drizzly November in my soul; whenever
            I find myself involuntarily pausing before coffin warehouses, and
            bringing up the rear of every funeral I meet; and especially whenever
            my hypos get such an upper hand of me, that it requires a strong moral
            principle to prevent me from deliberately stepping into the street, and
            methodically knocking people’s hats off—then, I account it high time to
            get to sea as soon as I can. This is my substitute for pistol and ball.
            With a philosophical flourish Cato throws himself upon his sword; I
            quietly take to the ship. There is nothing surprising in this. If they
            but knew it, almost all men in their degree, some time or other,
            cherish very nearly the same feelings towards the ocean with me.
            """;

    @Test
    public void helloWithSystemPrompt() {
        final AiAgentBody<TextContent> body = new AiAgentBody<TextContent>()
                .withSystemMessage("You are a gnome. Respond in rhymes and riddles.")
                .withUserMessage("Please summarize this text.")
                .withContent(TextContent.from(bigTextBlock));

        template.requestBody("direct:chat", body, String.class);
    }
}
