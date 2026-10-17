package com.example.repository;

import com.example.model.ChatSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatSessionRepository extends JpaRepository<ChatSession, Long> {

    Optional<ChatSession> findBySessionId(String sessionId);

    @Query("SELECT cs FROM ChatSession cs ORDER BY cs.updatedAt DESC")
    List<ChatSession> findAllOrderByUpdatedAtDesc();

    @Query("SELECT cs FROM ChatSession cs LEFT JOIN FETCH cs.messages WHERE cs.sessionId = :sessionId")
    Optional<ChatSession> findBySessionIdWithMessages(String sessionId);
}