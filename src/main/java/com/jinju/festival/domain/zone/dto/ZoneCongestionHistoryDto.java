package com.jinju.festival.domain.zone.dto;

import com.jinju.festival.domain.zone.entity.ZoneCongestionHistory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ZoneCongestionHistoryDto {

    private Long historyId;
    private Integer peopleCount;
    private String congestionLevel;
    private LocalDateTime createdAt;

    public static ZoneCongestionHistoryDto from(ZoneCongestionHistory history) {
        if (history == null) return null;
        return ZoneCongestionHistoryDto.builder()
                .historyId(history.getHistoryId())
                .peopleCount(history.getPeopleCount())
                .congestionLevel(history.getCongestionLevel())
                .createdAt(history.getCreatedAt())
                .build();
    }
}
