package com.jinju.festival.domain.parking.dto;

import com.jinju.festival.domain.parking.entity.ParkingStatusHistory;
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
public class ParkingStatusHistoryDto {

    private Long historyId;
    private Integer currentParkingCount;
    private Integer usedSpace;
    private Integer usedDisSpace;
    private Integer availableSpaces;
    private BigDecimal occupancyRate;
    private Boolean isFull;
    private String congestionStatus;
    private LocalDateTime createdAt;

    public static ParkingStatusHistoryDto from(ParkingStatusHistory history) {
        if (history == null) return null;
        return ParkingStatusHistoryDto.builder()
                .historyId(history.getHistoryId())
                .currentParkingCount(history.getCurrentParkingCount())
                .usedSpace(history.getUsedSpace())
                .usedDisSpace(history.getUsedDisSpace())
                .availableSpaces(history.getAvailableSpaces())
                .occupancyRate(history.getOccupancyRate())
                .isFull(history.getIsFull())
                .congestionStatus(history.getCongestionStatus())
                .createdAt(history.getCreatedAt())
                .build();
    }
}
