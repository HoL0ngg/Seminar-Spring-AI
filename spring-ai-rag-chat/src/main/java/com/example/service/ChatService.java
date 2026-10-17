package com.example.service;

import com.example.model.ChatMessage;
import com.example.model.ChatSession;
import com.example.repository.ChatMessageRepository;
import com.example.repository.ChatSessionRepository;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class ChatService {

    private final ChatClient chatClient;
    private final DocumentService documentService;
    private final ChatSessionRepository sessionRepository;
    private final ChatMessageRepository messageRepository;

    private static final String RAG_SYSTEM_PROMPT = """
            You are a helpful AI assistant for TechCorp customer support. Use the provided context 
            to answer questions accurately and helpfully. If you cannot find the answer in the context, 
            say so clearly and offer to help in other ways.
            
            Guidelines:
            - Be friendly and professional
            - Provide specific information when available
            - If context is insufficient, acknowledge limitations
            - Offer to escalate to human support when appropriate
            - Remember previous conversation context
            
            Context from knowledge base:
            {context}
            """;

    private static final String CONVERSATION_PROMPT = """
            Previous conversation:
            {conversation_history}
            
            Current question: {question}
            
            Please provide a helpful response based on the context and conversation history.
            """;

    public ChatService(ChatClient.Builder chatClientBuilder, 
                      DocumentService documentService,
                      ChatSessionRepository sessionRepository,
                      ChatMessageRepository messageRepository) {
        this.chatClient = chatClientBuilder.build();
        this.documentService = documentService;
        this.sessionRepository = sessionRepository;
        this.messageRepository = messageRepository;
    }

    public ChatSession createNewSession() {
        String sessionId = UUID.randomUUID().toString();
        String title = "New Chat " + java.time.LocalDateTime.now().format(
            java.time.format.DateTimeFormatter.ofPattern("MMM dd, HH:mm"));
        
        ChatSession session = new ChatSession(sessionId, title);
        return sessionRepository.save(session);
    }

    public ChatSession getSession(String sessionId) {
        return sessionRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found: " + sessionId));
    }

    public List<ChatSession> getAllSessions() {
        return sessionRepository.findAllOrderByUpdatedAtDesc();
    }

    public ChatResponse sendMessage(String sessionId, String userMessage) {
        ChatSession session = getSession(sessionId);
        
        // Save user message
        ChatMessage userMsg = new ChatMessage(ChatMessage.MessageType.USER, userMessage);
        session.addMessage(userMsg);
        
        // Get conversation history
        List<ChatMessage> recentMessages = messageRepository.findRecentMessagesBySessionId(
            sessionId, PageRequest.of(0, 10));
        
        // Search for relevant documents
        List<Document> relevantDocs = documentService.searchSimilarDocuments(userMessage, 5);
        String context = relevantDocs.stream()
                .map(Document::getContent)
                .collect(Collectors.joining("\n\n"));
        
        // Build conversation history
        String conversationHistory = recentMessages.stream()
                .map(msg -> msg.getType().name().toLowerCase() + ": " + msg.getContent())
                .collect(Collectors.joining("\n"));
        
        // Generate AI response
        PromptTemplate systemTemplate = new PromptTemplate(RAG_SYSTEM_PROMPT);
        String systemMessage = systemTemplate.render(Map.of("context", context));
        
        PromptTemplate conversationTemplate = new PromptTemplate(CONVERSATION_PROMPT);
        String conversationPrompt = conversationTemplate.render(Map.of(
                "conversation_history", conversationHistory,
                "question", userMessage
        ));
        
        List<Message> messages = new ArrayList<>();
        messages.add(new org.springframework.ai.chat.messages.SystemMessage(systemMessage));
        messages.add(new UserMessage(conversationPrompt));
        
        Prompt prompt = new Prompt(messages);
        String aiResponse = chatClient.prompt(prompt).call().content();
        
        // Save AI response
        List<String> sources = relevantDocs.stream()
                .map(doc -> doc.getMetadata().getOrDefault("source", "Unknown").toString())
                .distinct()
                .collect(Collectors.toList());
        
        ChatMessage aiMsg = new ChatMessage(ChatMessage.MessageType.ASSISTANT, aiResponse, 
                                          sources, relevantDocs.size());
        session.addMessage(aiMsg);
        
        sessionRepository.save(session);
        
        return new ChatResponse(aiResponse, sources, relevantDocs.size());
    }

    public void updateSessionTitle(String sessionId, String newTitle) {
        ChatSession session = getSession(sessionId);
        session.setTitle(newTitle);
        sessionRepository.save(session);
    }

    public void deleteSession(String sessionId) {
        ChatSession session = getSession(sessionId);
        sessionRepository.delete(session);
    }

    public List<ChatMessage> getSessionMessages(String sessionId) {
        return messageRepository.findBySessionIdOrderByCreatedAt(sessionId);
    }

    public record ChatResponse(String message, List<String> sources, int documentsUsed) {}
}