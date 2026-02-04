package org.apache.camel.example.basic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.guardrail.OutputGuardrail;
import dev.langchain4j.guardrail.OutputGuardrailResult;

public class DoNotRespondWithConanOutputGuardrail implements OutputGuardrail {
    private final Logger LOG = LoggerFactory.getLogger(getClass());

    @Override
    public OutputGuardrailResult validate(AiMessage responseFromLLM) {
        if (responseFromLLM.text().toLowerCase().contains("conan")) {
            return failure("Do not mention Conan.");
        }
        return success();
    }

}
