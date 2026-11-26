package com.jinju.festival.domain.zone.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TouristZoneDetailResponse {

    private TouristZoneDto zone;
    private List<ZoneCongestionHistoryDto> recentCongestionHistories;
}
