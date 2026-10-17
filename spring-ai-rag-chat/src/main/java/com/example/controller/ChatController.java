package com.example.controller;

import com.example.model.ChatMessage;
import com.example.model.ChatSession;
import com.example.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class ChatController {

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;

    @Autowired
    public ChatController(ChatService chatService, SimpMessagingTemplate messagingTemplate) {
        this.chatService = chatService;
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/chat.sendMessage")
    @SendTo("/topic/public")
    public ChatMessageDto sendMessage(ChatMessageDto chatMessage) {
        try {
            ChatService.ChatResponse response = chatService.sendMessage(
                chatMessage.sessionId(), chatMessage.content());
            
            return new ChatMessageDto(
                chatMessage.sessionId(),
                response.message(),
                "ASSISTANT",
                response.sources(),
                response.documentsUsed()
            );
        } catch (Exception e) {
            return new ChatMessageDto(
                chatMessage.sessionId(),
                "Sorry, I encountered an error processing your message: " + e.getMessage(),
                "ASSISTANT",
                List.of(),
                0
            );
        }
    }

    @PostMapping("/api/chat/sessions")
    @ResponseBody
    public ChatSession createSession() {
        return chatService.createNewSession();
    }

    @GetMapping("/api/chat/sessions")
    @ResponseBody
    public List<ChatSession> getSessions() {
        return chatService.getAllSessions();
    }

    @GetMapping("/api/chat/sessions/{sessionId}")
    @ResponseBody
    public ChatSession getSession(@PathVariable String sessionId) {
        return chatService.getSession(sessionId);
    }

    @GetMapping("/api/chat/sessions/{sessionId}/messages")
    @ResponseBody
    public List<ChatMessage> getSessionMessages(@PathVariable String sessionId) {
        return chatService.getSessionMessages(sessionId);
    }

    @PutMapping("/api/chat/sessions/{sessionId}/title")
    @ResponseBody
    public void updateSessionTitle(@PathVariable String sessionId, @RequestBody TitleUpdateRequest request) {
        chatService.updateSessionTitle(sessionId, request.title());
    }

    @DeleteMapping("/api/chat/sessions/{sessionId}")
    @ResponseBody
    public void deleteSession(@PathVariable String sessionId) {
        chatService.deleteSession(sessionId);
    }

    public record ChatMessageDto(
        String sessionId,
        String content,
        String type,
        List<String> sources,
        Integer documentsUsed
    ) {}

    public record TitleUpdateRequest(String title) {}
}