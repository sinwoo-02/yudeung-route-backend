package com.jinju.festival.domain.route.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "A* 최적 경로 추천 요청 DTO")
public class RouteRecommendRequest {

    // 1. 임의의 출발지 및 목적지 위경도 좌표 입력 방식
    @Schema(description = "출발지 위도", example = "35.1896")
    private Double originLatitude;

    @Schema(description = "출발지 경도", example = "128.0807")
    private Double originLongitude;

    @Schema(description = "목적지 위도", example = "35.1831")
    private Double destinationLatitude;

    @Schema(description = "목적지 경도", example = "128.0725")
    private Double destinationLongitude;

    // 2. 목적지가 주차장인 경우
    @Schema(description = "목적지 주차장 ID (옵션)", hidden = true)
    private Long destinationParkingId;

    // 3. 목적지가 관광/행사장 구역인 경우
    @Schema(description = "목적지 관광 구역 ID (옵션)", hidden = true)
    private Long destinationZoneId;

    // 4. 노드 ID 직접 지정 방식 (옵션)
    @Schema(description = "시작 노드 ID 직접 지정 (옵션)", hidden = true)
    private Long startNodeId;

    @Schema(description = "목적지 노드 ID 직접 지정 (옵션)", hidden = true)
    private Long goalNodeId;
}
