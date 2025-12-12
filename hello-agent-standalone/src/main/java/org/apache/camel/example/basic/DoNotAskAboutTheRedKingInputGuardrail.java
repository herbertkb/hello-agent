package org.apache.camel.example.basic;

import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.guardrail.InputGuardrail;
import dev.langchain4j.guardrail.InputGuardrailResult;

public class DoNotAskAboutTheRedKingInputGuardrail implements InputGuardrail {
    @Override
    public InputGuardrailResult validate(UserMessage request) {
        if (request.singleText().contains("Red King")) {
            return failure("Mentions the Red King, who must not be mentioned.");
        }
        return success();
    }
}
