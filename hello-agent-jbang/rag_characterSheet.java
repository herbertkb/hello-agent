//DEPS org.apache.camel:camel-bom:4.17.0-SNAPSHOT@pom
//DEPS org.apache.camel:camel-core
//DEPS org.apache.camel:camel-langchain4j-agent
//DEPS dev.langchain4j:langchain4j-ollama:1.9.1

import java.util.List;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.langchain4j.agent.api.Agent;
import org.apache.camel.component.langchain4j.agent.api.AgentConfiguration;
import org.apache.camel.component.langchain4j.agent.api.AgentWithoutMemory;
import org.apache.camel.component.langchain4j.agent.api.Headers;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.rag.DefaultRetrievalAugmentor;
import dev.langchain4j.rag.RetrievalAugmentor;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;

public class rag_characterSheet extends RouteBuilder {

    final String KNOWLEDGE_BASE = """
            CHARACTER SHEETS
            Name: Jimbo Swordman
            Class: Barbarian
            Species: Human
            Strength: 18
            Wisdom: 6

            Name: Mike Magicman
            Class: Wizard
            Species: Gnome
            Strength: 6
            Wisdom: 18
            """;

    @Override
    public void configure() throws Exception {

        ChatModel ollamaModel = OllamaChatModel.builder()
                .baseUrl("http://localhost:11434")
                .temperature(0.0)
                .logRequests(false)
                .logResponses(false)
                .modelName("granite4:1b")
                .build();

        // Create document from knowledge base
        Document document = Document.from(KNOWLEDGE_BASE);

        //
        InMemoryEmbeddingStore<TextSegment> embeddingStore = new InMemoryEmbeddingStore<>();
        EmbeddingStoreIngestor.ingest(List.of(document), embeddingStore);

        // Create content retriever
        EmbeddingStoreContentRetriever contentRetriever = EmbeddingStoreContentRetriever.builder()
                .embeddingStore(embeddingStore)
                .maxResults(3)
                .minScore(0.6)
                .build();

        // Create a RetrievalAugmentor that uses only a content retriever : naive rag
        // scenario
        RetrievalAugmentor retrievalAugmentor = DefaultRetrievalAugmentor.builder()
                .contentRetriever(contentRetriever)
                .build();

        // Create agent configuration
        AgentConfiguration configuration = new AgentConfiguration()
                .withRetrievalAugmentor(retrievalAugmentor)
                .withChatModel(ollamaModel);

        // Create the agent
        Agent simpleAgent = new AgentWithoutMemory(configuration);

        // Register the agent in the Camel context
        getContext().getRegistry().bind("simpleAgent", simpleAgent);

        // @formatter:off
        from("direct:agent")
            .routeId("agent")
            .to("langchain4j-agent:test-rag-agent?agent=#agentWithRag")
            .to("log:agent");

        from("timer:runOnce?repeatCount=1&delay=3000")
            .routeId("send one message")
            .setHeader(Headers.SYSTEM_MESSAGE)
                .constant("You are reading the character sheet for the user.")
            .setBody()
                .constant("Vividly describe Jimbo Swordman from the information in their sheet.")
            .to("direct:chat");
        // @formatter:on
    }
}
