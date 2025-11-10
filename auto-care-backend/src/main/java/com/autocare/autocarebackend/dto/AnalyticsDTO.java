package com.autocare.autocarebackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

public class AnalyticsDTO {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OverviewMetrics {
        private Long totalListings;
        private Long pendingApprovals;
        private Long approvedListings;
        private Double totalValue;
        private Long vehiclesWithLeasing;
        private Long vehiclesWithInsurance;
        private Double monthlyGrowthRate;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ManufacturerStats {
        private String manufacturer;
        private Long count;
        private Double percentage;
        private Double averagePrice;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TimeSeriesData {
        private String period;
        private Long newListings;
        private Long approvedListings;
        private Long pendingListings;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VehicleTypeStats {
        private String vehicleType;
        private Long count;
        private Double percentage;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StatusMetrics {
        private Long withLeasing;
        private Long withInsurance;
        private Long withBoth;
        private Long withNeither;
    }
}