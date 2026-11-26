package com.jinju.festival.domain.parking.controller;

import com.jinju.festival.domain.parking.dto.ParkingDetailResponse;
import com.jinju.festival.domain.parking.dto.ParkingDto;
import com.jinju.festival.domain.parking.service.ParkingService;
import com.jinju.festival.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "1. 실시간 주차장 API", description = "축제 연계 주차장 목록 및 공공데이터 기반 실시간 잔여면수/점유율 조회")
@RestController
@RequestMapping("/api/v1/parkings")
@RequiredArgsConstructor
public class ParkingController {

    private final ParkingService parkingService;

    @Operation(summary = "전체 주차장 목록 및 실시간 상태 조회", description = "유등축제 연계 전체 주차장 목록과 가장 최근 기록된 잔여면수, 점유율, 만차 여부, 혼잡상태를 조회합니다.")
    @GetMapping
    public ApiResponse<List<ParkingDto>> getAllParkings() {
        List<ParkingDto> parkings = parkingService.getAllParkingsWithLatestStatus();
        return ApiResponse.success("전체 주차장 목록 및 실시간 잔여면수를 조회했습니다.", parkings);
    }

    @Operation(summary = "특정 주차장 상세 및 최근 이력 조회", description = "주차장 ID(예: 1)에 해당하는 기본 정보와 최근 실시간 수집 이력 목록을 조회합니다.")
    @GetMapping("/{parkingId}")
    public ApiResponse<ParkingDetailResponse> getParkingDetail(
            @Parameter(description = "주차장 고유 ID (예: 1, 2 등)", example = "1")
            @PathVariable("parkingId") Long parkingId) {
        ParkingDetailResponse detail = parkingService.getParkingDetail(parkingId);
        return ApiResponse.success("주차장 상세 및 실시간 상태 정보를 조회했습니다.", detail);
    }
}
