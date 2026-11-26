package com.jinju.festival.domain.route.controller;

import com.jinju.festival.domain.route.dto.RouteNodeDto;
import com.jinju.festival.domain.route.dto.RouteRecommendRequest;
import com.jinju.festival.domain.route.dto.RouteRecommendResponse;
import com.jinju.festival.domain.route.service.RouteRecommendationService;
import com.jinju.festival.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "3. 최적 경로 추천 API (A* 알고리즘)", description = "CCTV 혼잡도 가중치를 반영한 A* 최적 경로 추천 및 도로망 노드 조회")
@RestController
@RequestMapping("/api/v1/routes")
@RequiredArgsConstructor
public class RouteController {

    private final RouteRecommendationService routeRecommendationService;

    @Operation(summary = "CCTV 혼잡도 기반 A* 최적 경로 추천 (POST)",
               description = "출발지(위경도), 목적지(위경도/주차장/구역)를 JSON 바디로 전달받아 혼잡 구역을 우회하는 최적 경로를 추천합니다.")
    @PostMapping("/recommend")
    public ApiResponse<RouteRecommendResponse> recommendRoutePost(@RequestBody RouteRecommendRequest request) {
        RouteRecommendResponse response = routeRecommendationService.recommendRoute(request);
        return ApiResponse.success("CCTV 혼잡도가 반영된 A* 최적 경로를 추천했습니다.", response);
    }

    @Operation(summary = "CCTV 혼잡도 기반 A* 최적 경로 추천 (GET 쿼리 파라미터)",
               description = "GET 요청의 쿼리 파라미터를 통해 출발지/목적지 위경도 또는 주차장 ID를 전달하여 최적 경로를 조회합니다.")
    @GetMapping("/recommend")
    public ApiResponse<RouteRecommendResponse> recommendRouteGet(
            @Parameter(description = "출발지 위도", example = "35.1896")
            @RequestParam(value = "originLat", required = false) Double originLat,
            @Parameter(description = "출발지 경도", example = "128.0807")
            @RequestParam(value = "originLon", required = false) Double originLon,
            @Parameter(description = "목적지 위도", example = "35.1831")
            @RequestParam(value = "destLat", required = false) Double destLat,
            @Parameter(description = "목적지 경도", example = "128.0725")
            @RequestParam(value = "destLon", required = false) Double destLon,
            @Parameter(description = "목적지 주차장 ID (지정 시 주차장 좌표로 자동 매핑)", example = "1")
            @RequestParam(value = "parkingId", required = false) Long parkingId,
            @Parameter(description = "목적지 관광 구역 ID (지정 시 구역 좌표로 자동 매핑)", example = "3")
            @RequestParam(value = "zoneId", required = false) Long zoneId,
            @Parameter(description = "시작 노드 ID 직접 지정 (옵션)", example = "1")
            @RequestParam(value = "startNodeId", required = false) Long startNodeId,
            @Parameter(description = "목적 노드 ID 직접 지정 (옵션)", example = "5")
            @RequestParam(value = "goalNodeId", required = false) Long goalNodeId) {

        RouteRecommendRequest request = RouteRecommendRequest.builder()
                .originLatitude(originLat)
                .originLongitude(originLon)
                .destinationLatitude(destLat)
                .destinationLongitude(destLon)
                .destinationParkingId(parkingId)
                .destinationZoneId(zoneId)
                .startNodeId(startNodeId)
                .goalNodeId(goalNodeId)
                .build();

        RouteRecommendResponse response = routeRecommendationService.recommendRoute(request);
        return ApiResponse.success("CCTV 혼잡도가 반영된 A* 최적 경로를 추천했습니다.", response);
    }

    @Operation(summary = "전체 도로망 노드 목록 조회",
               description = "경로 추천 네트워크를 구성하는 전체 노드(좌표 및 구역명 포함)를 조회합니다. 프론트엔드 지도 마커 표시에 활용됩니다.")
    @GetMapping("/nodes")
    public ApiResponse<List<RouteNodeDto>> getAllNodes() {
        List<RouteNodeDto> dtos = routeRecommendationService.getAllRouteNodes();
        return ApiResponse.success("도로망 전체 노드 목록을 조회했습니다.", dtos);
    }
}
