package com.autocare.autocarebackend.repository;

import com.autocare.autocarebackend.models.Conversation;
import com.autocare.autocarebackend.models.Message;
import com.autocare.autocarebackend.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findByConversationIdOrderByCreatedAtAsc(Long conversationId);

    Message findFirstByConversationIdOrderByCreatedAtDesc(Long conversationId);

    Long countByConversationIdAndSenderTypeAndIsRead(Long conversationId, String senderType, Boolean isRead);

    @Query("SELECT COUNT(m) FROM Message m WHERE m.conversationId IN ?1 AND m.senderType = ?2 AND m.isRead = false")
    Long countUnreadMessagesByConversationIdsAndSenderType(List<Long> conversationIds, String senderType);

    @Modifying
    @Transactional
    @Query("UPDATE Message m SET m.isRead = true WHERE m.conversationId = ?1 AND m.senderType = ?2 AND m.isRead = false")
    void markMessagesAsRead(Long conversationId, String senderType);

    Message findTopByConversationIdOrderByCreatedAtDesc(Long conversationId);

    long countByConversationIdAndSenderIdNotAndIsRead(Long conversationId, Long senderId, boolean isRead);
}