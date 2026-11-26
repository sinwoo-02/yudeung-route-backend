package com.jinju.festival.domain.zone.controller;

import com.jinju.festival.domain.zone.dto.AiCongestionSaveResponse;
import com.jinju.festival.domain.zone.dto.AiVideoCongestionReportRequest;
import com.jinju.festival.domain.zone.dto.TouristZoneDetailResponse;
import com.jinju.festival.domain.zone.dto.TouristZoneDto;
import com.jinju.festival.domain.zone.service.TouristZoneService;
import com.jinju.festival.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "2. CCTV 구역 및 혼잡도 API", description = "CCTV 분석 대상 관광 구역 및 실시간 인원수/혼잡도 레벨 조회 및 수신")
@RestController
@RequestMapping("/api/v1/zones")
@RequiredArgsConstructor
public class TouristZoneController {

    private final TouristZoneService touristZoneService;

    @Operation(summary = "전체 관광 구역 목록 및 실시간 혼잡도 조회", description = "진주성, 촉석루, 음악분수대, 망경동 둔치 등 주요 관광 구역의 실시간 인원수 및 혼잡도 가중치를 조회합니다.")
    @GetMapping
    public ApiResponse<List<TouristZoneDto>> getAllZones() {
        List<TouristZoneDto> zones = touristZoneService.getAllZonesWithLatestCongestion();
        return ApiResponse.success("전체 관광 구역 목록 및 실시간 CCTV 혼잡도를 조회했습니다.", zones);
    }

    @Operation(summary = "특정 관광 구역 상세 및 최근 혼잡도 이력 조회", description = "구역 ID에 해당하는 위치 좌표와 최근 측정된 시간대별 혼잡도 이력 목록을 조회합니다.")
    @GetMapping("/{zoneId}")
    public ApiResponse<TouristZoneDetailResponse> getZoneDetail(
            @Parameter(description = "관광 구역 고유 ID (예: 1, 2 등)", example = "1")
            @PathVariable("zoneId") Long zoneId) {
        TouristZoneDetailResponse detail = touristZoneService.getZoneDetail(zoneId);
        return ApiResponse.success("관광 구역 상세 및 실시간 혼잡도 정보를 조회했습니다.", detail);
    }

    @Operation(
            summary = "AI 서버 CCTV 영상 분석 결과 혼잡도 데이터 수신 및 저장",
            description = "AI 영상 분석 서버로부터 수신된 분석 결과(인원수, 상태 등)를 받아 거리 혼잡도 이력 테이블(zone_congestion_history)에 영상당 1건씩 저장합니다."
    )
    @PostMapping("/congestion")
    public ApiResponse<AiCongestionSaveResponse> receiveAiCongestionReports(
            @RequestBody List<AiVideoCongestionReportRequest> requests) {
        AiCongestionSaveResponse response = touristZoneService.saveAiCongestionReports(requests);
        return ApiResponse.success("AI 서버 CCTV 혼잡도 분석 데이터를 성공적으로 저장했습니다.", response);
    }
}
