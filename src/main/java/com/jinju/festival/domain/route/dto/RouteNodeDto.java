package com.jinju.festival.domain.route.dto;

import com.jinju.festival.domain.route.entity.RouteNode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RouteNodeDto {

    private Long nodeId;
    private String nodeName;
    private Double latitude;
    private Double longitude;
    private Long zoneId;
    private String zoneName;
    private String congestionLevel;
    private Double congestionMultiplier;

    public static RouteNodeDto from(RouteNode node, String congestionLevel, Double multiplier) {
        Long zoneId = (node.getTouristZone() != null) ? node.getTouristZone().getZoneId() : null;
        String zoneName = (node.getTouristZone() != null) ? node.getTouristZone().getZoneName() : null;

        return RouteNodeDto.builder()
                .nodeId(node.getNodeId())
                .nodeName(node.getNodeName())
                .latitude(node.getLatitude())
                .longitude(node.getLongitude())
                .zoneId(zoneId)
                .zoneName(zoneName)
                .congestionLevel(congestionLevel != null ? congestionLevel : "원활")
                .congestionMultiplier(multiplier != null ? multiplier : 1.0)
                .build();
    }
}
