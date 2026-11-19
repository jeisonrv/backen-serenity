package com.serenity.chat.application.port.out;

import java.util.List;
import com.serenity.chat.domain.model.MensajeChat;

/**
 * Port for AI-powered response generation.
 * Implementations should call an external LLM service (e.g., OpenAI) and return a textual response.
 */
public interface ChatAiProviderPort {
    /**
     * Generate a response based on the latest user message and the conversation history.
     *
     * @param userMessage the latest message sent by the user
     * @param history previous messages in the conversation (ordered chronologically)
     * @return generated reply content
     */
    String generateResponse(String userMessage, List<MensajeChat> history);
}
