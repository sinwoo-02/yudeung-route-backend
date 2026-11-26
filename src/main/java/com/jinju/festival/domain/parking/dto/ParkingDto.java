package com.jinju.festival.domain.parking.dto;

import com.jinju.festival.domain.parking.entity.Parking;
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
public class ParkingDto {

    private Long parkingId;
    private String externalCode;
    private Long festivalId;
    private String name;
    private String address;
    private String parkingType;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private Integer totalSpace;
    private Integer disSpace;

    // 최신 실시간 현황 정보
    private Integer currentParkingCount;
    private Integer availableSpaces;
    private Integer usedDisSpace;
    private BigDecimal occupancyRate;
    private Boolean isFull;
    private String congestionStatus;
    private LocalDateTime statusUpdatedAt;

    public static ParkingDto from(Parking parking, ParkingStatusHistory latestStatus) {
        Long festivalId = (parking.getFestival() != null) ? parking.getFestival().getFestivalId() : null;

        ParkingDtoBuilder builder = ParkingDto.builder()
                .parkingId(parking.getParkingId())
                .externalCode(parking.getExternalCode())
                .festivalId(festivalId)
                .name(parking.getName())
                .address(parking.getAddress())
                .parkingType(parking.getParkingType())
                .latitude(parking.getLatitude())
                .longitude(parking.getLongitude())
                .totalSpace(parking.getTotalSpace())
                .disSpace(parking.getDisSpace());

        if (latestStatus != null) {
            builder.currentParkingCount(latestStatus.getCurrentParkingCount())
                    .availableSpaces(latestStatus.getAvailableSpaces())
                    .usedDisSpace(latestStatus.getUsedDisSpace())
                    .occupancyRate(latestStatus.getOccupancyRate())
                    .isFull(latestStatus.getIsFull())
                    .congestionStatus(latestStatus.getCongestionStatus())
                    .statusUpdatedAt(latestStatus.getCreatedAt());
        } else {
            builder.currentParkingCount(0)
                    .availableSpaces(parking.getTotalSpace())
                    .usedDisSpace(0)
                    .occupancyRate(BigDecimal.ZERO)
                    .isFull(false)
                    .congestionStatus("원활")
                    .statusUpdatedAt(parking.getUpdatedAt() != null ? parking.getUpdatedAt() : parking.getCreatedAt());
        }

        return builder.build();
    }
}
