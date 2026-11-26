package com.jinju.festival.domain.zone.dto;

import com.jinju.festival.domain.route.service.RouteRecommendationService;
import com.jinju.festival.domain.zone.entity.TouristZone;
import com.jinju.festival.domain.zone.entity.ZoneCongestionHistory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TouristZoneDto {

    private Long zoneId;
    private Long festivalId;
    private String zoneName;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private Integer coverageRadiusMeters;

    // 실시간 CCTV 혼잡도 정보
    private Integer peopleCount;
    private String congestionLevel;
    private Double congestionMultiplier;
    private LocalDateTime statusUpdatedAt;

    public static TouristZoneDto from(TouristZone zone, ZoneCongestionHistory latestHistory) {
        Long festivalId = (zone.getFestival() != null) ? zone.getFestival().getFestivalId() : null;

        TouristZoneDtoBuilder builder = TouristZoneDto.builder()
                .zoneId(zone.getZoneId())
                .festivalId(festivalId)
                .zoneName(zone.getZoneName())
                .latitude(zone.getLatitude())
                .longitude(zone.getLongitude())
                .coverageRadiusMeters(zone.getCoverageRadiusMeters());

        if (latestHistory != null) {
            String level = latestHistory.getCongestionLevel();
            double multiplier = RouteRecommendationService.getCongestionMultiplier(level);
            builder.peopleCount(latestHistory.getPeopleCount())
                    .congestionLevel(level)
                    .congestionMultiplier(multiplier)
                    .statusUpdatedAt(latestHistory.getCreatedAt());
        } else {
            builder.peopleCount(0)
                    .congestionLevel("원활")
                    .congestionMultiplier(1.0)
                    .statusUpdatedAt(zone.getCreatedAt());
        }

        return builder.build();
    }
}
