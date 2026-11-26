package com.jinju.festival.domain.route.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RouteRecommendResponse {

    private LocationPoint origin;
    private LocationPoint destination;

    private RouteNodeDto startNode;
    private RouteNodeDto goalNode;

    private List<RouteNodeDto> pathNodes;

    private Double totalDistanceMeters;
    private Double totalWeightedCost;

    private Integer estimatedWalkingTimeMinutes;
    private Integer estimatedDrivingTimeMinutes;

    private String congestionSummary;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LocationPoint {
        private Double latitude;
        private Double longitude;
        private String description;
    }
}
