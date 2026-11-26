package com.jinju.festival.domain.parking.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParkingDetailResponse {

    private ParkingDto parking;
    private List<ParkingStatusHistoryDto> recentStatusHistories;
}
