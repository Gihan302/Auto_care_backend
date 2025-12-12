package com.autocare.autocarebackend.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentStatsDTO {
    private long totalAds;
    private long activeAds;
    private long soldAds;
    private String packageUsage; // e.g., "25/50"
    private String packageDaysLeft; // e.g., "15 days left"
}
