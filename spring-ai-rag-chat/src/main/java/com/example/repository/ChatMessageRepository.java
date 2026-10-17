package com.example.repository;

import com.example.model.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    @Query("SELECT cm FROM ChatMessage cm WHERE cm.chatSession.sessionId = :sessionId ORDER BY cm.createdAt ASC")
    List<ChatMessage> findBySessionIdOrderByCreatedAt(String sessionId);

    @Query("SELECT cm FROM ChatMessage cm WHERE cm.chatSession.sessionId = :sessionId ORDER BY cm.createdAt DESC")
    List<ChatMessage> findRecentMessagesBySessionId(String sessionId, org.springframework.data.domain.Pageable pageable);
}