//DEPS org.apache.camel:camel-bom:4.17.0-SNAPSHOT@pom
//DEPS org.apache.camel:camel-core
//DEPS org.apache.camel:camel-langchain4j-agent
//DEPS dev.langchain4j:langchain4j-ollama:1.9.1

import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.langchain4j.agent.api.Agent;
import org.apache.camel.component.langchain4j.agent.api.AgentConfiguration;
import org.apache.camel.component.langchain4j.agent.api.AgentWithoutMemory;
import org.apache.camel.component.langchain4j.agent.api.AiAgentBody;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.store.memory.chat.InMemoryChatMemoryStore;

public class toolMethodsWithMemory_combat extends RouteBuilder {

    @Override
    public void configure() throws Exception {

        ChatModel ollamaModel = OllamaChatModel.builder()
                .baseUrl("http://localhost:11434")
                .temperature(0.0)
                .logRequests(false)
                .logResponses(false)
                .modelName("granite4:1b")
                .build();

        // Example of creating a Chat Memory Provider : Create a message window memory that keeps the last 10 messages
        ChatMemoryProvider chatMemoryProvider = memoryId -> MessageWindowChatMemory.builder()
                .id(memoryId)
                .maxMessages(10)
                .chatMemoryStore(new InMemoryChatMemoryStore())
                .build();

        // Create agent configuration
        AgentConfiguration configuration = new AgentConfiguration()
                .withChatModel(ollamaModel)
                .withChatMemoryProvider(chatMemoryProvider)
                .withCustomTools(List.of(new CombatTool()));

        // Create the agent
        Agent simpleAgent = new AgentWithoutMemory(configuration);

        // Register the agent in the Camel context
        getContext().getRegistry().bind("simpleAgent", simpleAgent);

        // @formatter:off
        from("direct:adventure")
            // .to("log:adventure")
            .to("langchain4j-agent:test?agent=#simpleAgent&tags=combat")
            .to("log:adventure");

        from("timer:runOnce?repeatCount=1&delay=3000")
            .setBody().constant(new AiAgentBody<>("I swing my mace at the skeleton", 
                    """
                    You are a text-based adventure set in a ruined castle.
                    There is a skeleton advancing towards me.
                    In the style of a fantasy writer, describe the actions and result of combat using tools to calculate the damage.
                    Describe the impact of the mace on the skeleton's bones.
                    My weapon deals some number of damage points.
                    Those damage points are subtracted from the hit points of an enemy.
                    The skeleton has 10 hit points.
                    The skeleton will crumple to dust if it has less than 1 hit point.
                    The skeleton is still standing if it has atleast 1 hit point.                    
                    """, 
                    "session_01"))
            .log("ROUND 1")
            .to("direct:adventure")
            .setBody().constant(new AiAgentBody<>("I swing my mace again at the skeleton", 
                    """
                You are a text-based adventure set in a ruined castle.
                The skeleton is still standing before me.
                In the style of a fantasy writer, describe the actions and result of combat using tools to calculate the damage.
                Describe the impact of the mace on the skeleton's bones.
                My weapon deals some number of damage points.
                Those damage points are subtracted from the hit points of an enemy.
                The skeleton will crumple to dust if it has less than 1 hit point.
                The skeleton is still standing if it has atleast 1 hit point.                   
                    """, 
                    "session_01"))
            .log("ROUND 2")
            .to("direct:adventure")
            ;
        // @formatter:on
    }

    public class CombatTool {

        Map<String, Integer> weaponDamage = Map.of(
                "knife", 4,
                "mace", 8,
                "sword", 10);

        Random dice = new Random();

        @Tool("Calculate damage delt to enemy")
        public int damage(@P("Weapon name") String weapon) {
            // return dice.nextInt(weaponDamage.get(weapon)) + 1;
            return 1;
        }

        @Tool("Reduce an enemies hit points from damage delt by a weapon")
        public int attack(@P("Enemy hit points") int enemyHP, @P("Weapon") String weapon) {
            return enemyHP - damage(weapon);
        }

        // @Tool("Enemy hit points after damage delt by a weapon")
        // public int attack(@P("Enemy hit points") int enemyHP, @P("Damage delt") int
        // damage) {
        // return enemyHP - damage;
        // }

    }
}
