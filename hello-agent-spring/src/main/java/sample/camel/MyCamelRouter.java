package sample.camel;

import java.util.List;

import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.rest.RestBindingMode;
import org.apache.camel.component.langchain4j.agent.api.Agent;
import org.apache.camel.component.langchain4j.agent.api.AgentConfiguration;
import org.apache.camel.component.langchain4j.agent.api.AgentWithoutMemory;
import org.apache.camel.component.langchain4j.agent.api.AiAgentBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import dev.langchain4j.model.openai.OpenAiChatModel;
import jakarta.annotation.PostConstruct;

@Component
public class MyCamelRouter extends RouteBuilder {

    @Autowired
    CamelContext context;

    @Value("${app.base.url}")
    String baseUrl;
    @Value("${app.api.key}")
    String apiKey;
    @Value("${app.model.name}")
    String modelName;

    @PostConstruct
    void setup() {
        // demo model setup:
        // https://docs.langchain4j.dev/integrations/language-models/open-ai
        // OpenAiChatModel model = OpenAiChatModel.builder()
        // .baseUrl("http://langchain4j.dev/demo/openai/v1")
        // .apiKey("demo")
        // .modelName("gpt-4o-mini")
        // .build();

        OpenAiChatModel model = OpenAiChatModel.builder()
                .baseUrl(baseUrl)
                .apiKey(apiKey)
                .modelName(modelName)
                .build();

        // Create agent configuration
        AgentConfiguration configuration = new AgentConfiguration()
                .withChatModel(model)
                .withInputGuardrailClasses(List.of())
                .withOutputGuardrailClasses(List.of());

        // Create the agent
        Agent simpleAgent = new AgentWithoutMemory(configuration);

        // Register the agent in the Camel context
        context.getRegistry().bind("simpleAgent", simpleAgent);
    }

    @Override
    public void configure() throws Exception {
        // @formatter:off
        from("direct:chat")
            .to("log:chat")
            .to("langchain4j-agent:test?agent=#simpleAgent")
            .to("log:chat");

        restConfiguration()
            .bindingMode(RestBindingMode.json);

        rest("/chat").description("Chat REST service")
            .consumes("application/json")
            .produces("application/json")

            .post()
                .type(AiAgentBody.class)
                .to("direct:chat");
        // @formatter:on
    }

}
