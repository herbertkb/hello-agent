package org.apache.camel.example.basic;

import java.util.Map;
import java.util.Random;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;

public class CombatTool {

    Map<String, Integer> weaponDamage = Map.of(
        "knife", 4,
        "mace", 8,
        "sword", 10
    );

    Random dice = new Random();

    @Tool("Calculate damage delt to enemy")
    public int damage(@P("Weapon name") String weapon) {
        return dice.nextInt(weaponDamage.get(weapon)) + 1;
        // return 6;
    }

    @Tool("Reduce an enemies hit points from damage delt by a weapon")
    public int attack(@P("Enemy hit points") int enemyHP, @P("Weapon") String weapon) {
        return enemyHP - damage(weapon);
    }

    // @Tool("Enemy hit points after damage delt by a weapon")
    // public int attack(@P("Enemy hit points") int enemyHP, @P("Damage delt") int damage) {
    //     return enemyHP - damage;
    // }

}
