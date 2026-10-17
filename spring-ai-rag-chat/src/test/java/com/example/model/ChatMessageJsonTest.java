package com.example.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class ChatMessageJsonTest {

    @Test
    void serializesChatHistoryWithoutFollowingTheSessionBackReference() throws Exception {
        ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();
        ChatSession session = new ChatSession("test-session", "Test");
        ChatMessage message = new ChatMessage(ChatMessage.MessageType.USER, "Hello");
        session.addMessage(message);

        var sessionJson = mapper.readTree(mapper.writeValueAsString(session));
        assertEquals("Hello", sessionJson.path("messages").get(0).path("content").asText());
        assertFalse(sessionJson.path("messages").get(0).has("chatSession"));

        var messagesJson = mapper.readTree(mapper.writeValueAsString(List.of(message)));
        assertEquals(1, messagesJson.size());
        assertFalse(messagesJson.get(0).has("chatSession"));
    }
}
