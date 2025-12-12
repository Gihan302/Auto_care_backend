package com.autocare.autocarebackend.repository;

import com.autocare.autocarebackend.models.Conversation;
import com.autocare.autocarebackend.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {
    List<Conversation> findByUserIdOrderByUpdatedAtDesc(Long userId);

    List<Conversation> findByUserId(Long userId);

    Optional<Conversation> findByUserIdAndCompanyName(Long userId, String companyName);

    Optional<Conversation> findByUserIdAndAgentId(Long userId, Long agentId);

    List<Conversation> findByCompanyNameOrderByUpdatedAtDesc(String companyName);

    List<Conversation> findByCompanyName(String companyName);

    List<Conversation> findByAgentIdOrderByUpdatedAtDesc(Long agentId);

    List<Conversation> findByAgentId(Long agentId); // Re-added

    @Modifying
    @Transactional
    @Query("UPDATE Conversation c SET c.updatedAt = CURRENT_TIMESTAMP WHERE c.id = ?1")
    void updateTimestamp(Long conversationId);
}