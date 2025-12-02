package sample.camel;

import java.util.List;

import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.langchain4j.agent.api.Agent;
import org.apache.camel.component.langchain4j.agent.api.AgentConfiguration;
import org.apache.camel.component.langchain4j.agent.api.AgentWithoutMemory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import dev.langchain4j.model.openai.OpenAiChatModel;
import jakarta.annotation.PostConstruct;


@Component
public class MyCamelRouter extends RouteBuilder {

    @Autowired
    CamelContext context;

    @PostConstruct
    void setup() {
        // demo model setup: https://docs.langchain4j.dev/integrations/language-models/open-ai
        OpenAiChatModel model = OpenAiChatModel.builder()
            .baseUrl("http://langchain4j.dev/demo/openai/v1")
            .apiKey("demo")
            .modelName("gpt-4o-mini")
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
        from("direct:chat")
            .to("log:chat")
            .to("langchain4j-agent:test?agent=#simpleAgent")
            .to("log:chat");
    }

}


