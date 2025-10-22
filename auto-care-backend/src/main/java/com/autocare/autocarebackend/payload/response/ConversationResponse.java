package com.autocare.autocarebackend.payload.response;

import java.time.LocalDateTime;

public class ConversationResponse {
    private Long id;
    private String companyName;
    private String companyType;
    private String status;
    private String lastMessage;
    private LocalDateTime lastMessageTime;
    private Long unreadCount;

    public ConversationResponse(Long id, String companyName, String companyType, String status,
                                String lastMessage, LocalDateTime lastMessageTime, Long unreadCount) {
        this.id = id;
        this.companyName = companyName;
        this.companyType = companyType;
        this.status = status;
        this.lastMessage = lastMessage;
        this.lastMessageTime = lastMessageTime;
        this.unreadCount = unreadCount;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    public String getCompanyType() { return companyType; }
    public void setCompanyType(String companyType) { this.companyType = companyType; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getLastMessage() { return lastMessage; }
    public void setLastMessage(String lastMessage) { this.lastMessage = lastMessage; }

    public LocalDateTime getLastMessageTime() { return lastMessageTime; }
    public void setLastMessageTime(LocalDateTime lastMessageTime) { this.lastMessageTime = lastMessageTime; }

    public Long getUnreadCount() { return unreadCount; }
    public void setUnreadCount(Long unreadCount) { this.unreadCount = unreadCount; }
}