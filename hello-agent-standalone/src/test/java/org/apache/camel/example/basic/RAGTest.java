package org.apache.camel.example.basic;

import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiPredicate;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.langchain4j.agent.api.Agent;
import org.apache.camel.component.langchain4j.agent.api.AgentConfiguration;
import org.apache.camel.component.langchain4j.agent.api.AgentWithoutMemory;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.spi.Registry;
import org.apache.camel.test.junit5.CamelTestSupport;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.mcp.client.DefaultMcpClient;
import dev.langchain4j.mcp.client.McpClient;
import dev.langchain4j.mcp.client.transport.McpTransport;
import dev.langchain4j.mcp.client.transport.stdio.StdioMcpTransport;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.rag.DefaultRetrievalAugmentor;
import dev.langchain4j.rag.RetrievalAugmentor;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import static org.apache.camel.component.langchain4j.agent.api.Headers.SYSTEM_MESSAGE;

public class RAGTest extends CamelTestSupport {
        private final Logger LOG = LoggerFactory.getLogger(getClass());

        final String KNOWLEDGE_BASE = """
                        CHARACTER SHEET
                        Name: Jimbo Swordman
                        Class: Barbarian
                        Species: Human
                        Strength: 18
                        Wisdom: 6
                        """;

        @Test
        // @Disabled
        public void rag() throws InterruptedException, URISyntaxException {
                MockEndpoint mock = getMockEndpoint("mock:output");
                mock.expectedMessageCount(1);

                var systemPrompt = "You are reading the character sheet for the user.";
                // var userPrompt = "What is my character's name, class, and strength?";
                var userPrompt = "Vividly describe my character from the information in his sheet.";

                String response = template.requestBodyAndHeader("direct:agent-with-rag", userPrompt, SYSTEM_MESSAGE,
                                systemPrompt,
                                String.class);
                LOG.info("Response: {}", response);

                mock.assertIsSatisfied(10L * 1000L);
        }

        @Override
        protected void bindToRegistry(Registry registry) throws Exception {

                // Register the agent in the Camel context
                ChatModel chatModel = OllamaChatModel.builder()
                                .baseUrl("http://localhost:11434")
                                .temperature(0.0)
                                .logRequests(true)
                                .logResponses(true)
                                .modelName("granite4:1b")
                                .build();

                // // Create document from knowledge base
                Document document = Document.from(KNOWLEDGE_BASE);

                // // Split into chunks
                // List<TextSegment> segments = DocumentSplitters.recursive(300,
                // 100).split(document);
                InMemoryEmbeddingStore<TextSegment> embeddingStore = new InMemoryEmbeddingStore<>();
                EmbeddingStoreIngestor.ingest(List.of(document), embeddingStore);

                // Create content retriever
                EmbeddingStoreContentRetriever contentRetriever = EmbeddingStoreContentRetriever.builder()
                                .embeddingStore(embeddingStore)
                                // .embeddingModel(embeddingModel)
                                .maxResults(3)
                                .minScore(0.6)
                                .build();

                // Create a RetrievalAugmentor that uses only a content retriever : naive rag
                // scenario
                RetrievalAugmentor retrievalAugmentor = DefaultRetrievalAugmentor.builder()
                                .contentRetriever(contentRetriever)
                                .build();

                // Create agent configuration with RAG support
                AgentConfiguration configuration = new AgentConfiguration()
                                .withChatModel(chatModel)
                                .withRetrievalAugmentor(retrievalAugmentor)
                                .withInputGuardrailClasses(List.of())
                                .withOutputGuardrailClasses(List.of());

                Agent agentWithRag = new AgentWithoutMemory(configuration);

                // Register agent in the context
                this.context.getRegistry().bind("agentWithRag", agentWithRag);
        }

        @Override
        protected RouteBuilder createRouteBuilder() {
                return new RouteBuilder() {
                        public void configure() {
                                from("direct:agent-with-rag")
                                                .to("log:rag")
                                                .to("langchain4j-agent:test-rag-agent?agent=#agentWithRag")
                                                .to("log:rag")
                                                .to("mock:output");
                        }
                };
        }
}
