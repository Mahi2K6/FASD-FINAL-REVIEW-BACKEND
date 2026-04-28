package com.medconnect.backend.repository;

import com.medconnect.backend.model.AiConversationLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AiConversationLogRepository extends JpaRepository<AiConversationLog, Long> {
    List<AiConversationLog> findByUserEmailOrderByCreatedAtDesc(String userEmail);
}
