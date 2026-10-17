package com.example.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "chat_messages")
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private ChatSession chatSession;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MessageType type;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @ElementCollection
    @CollectionTable(name = "message_sources", joinColumns = @JoinColumn(name = "message_id"))
    @Column(name = "source")
    private List<String> sources;

    @Column(name = "documents_used")
    private Integer documentsUsed;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public ChatMessage() {
        this.createdAt = LocalDateTime.now();
    }

    public ChatMessage(MessageType type, String content) {
        this();
        this.type = type;
        this.content = content;
    }

    public ChatMessage(MessageType type, String content, List<String> sources, Integer documentsUsed) {
        this(type, content);
        this.sources = sources;
        this.documentsUsed = documentsUsed;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ChatSession getChatSession() {
        return chatSession;
    }

    public void setChatSession(ChatSession chatSession) {
        this.chatSession = chatSession;
    }

    public MessageType getType() {
        return type;
    }

    public void setType(MessageType type) {
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public List<String> getSources() {
        return sources;
    }

    public void setSources(List<String> sources) {
        this.sources = sources;
    }

    public Integer getDocumentsUsed() {
        return documentsUsed;
    }

    public void setDocumentsUsed(Integer documentsUsed) {
        this.documentsUsed = documentsUsed;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public enum MessageType {
        USER, ASSISTANT, SYSTEM
    }
}