package com.autocare.autocarebackend.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentActivityDTO {
    private Long id;
    private String user; // Can be the agent ('You') or another user (e.g., 'Buyer123')
    private String action; // e.g., "sent an inquiry on 'Toyota Camry 2020'"
    private Date time;
    private String status; // e.g., "New Message", "Sold", "Active"
    private String initials; // e.g., "B", "Y"
    private String avatarColor; // e.g., "bg-blue-500"
    private String link; // Optional link to the relevant page
}
