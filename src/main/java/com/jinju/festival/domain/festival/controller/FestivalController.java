package com.jinju.festival.domain.festival.controller;

import com.jinju.festival.domain.festival.dto.FestivalDto;
import com.jinju.festival.domain.festival.service.FestivalService;
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

@Tag(name = "4. 축제 기본 정보 API", description = "진주시 유등축제 행사 기본 정보(기간, 장소, 소개 등) 조회")
@RestController
@RequestMapping("/api/v1/festivals")
@RequiredArgsConstructor
public class FestivalController {

    private final FestivalService festivalService;

    @Operation(summary = "전체 축제 목록 조회", description = "시스템에 등록된 축제 목록(축제명, 개최 기간, 위치 등)을 조회합니다.")
    @GetMapping
    public ApiResponse<List<FestivalDto>> getAllFestivals() {
        return ApiResponse.success("축제 목록을 조회했습니다.", festivalService.getAllFestivals());
    }

    @Operation(summary = "특정 축제 상세 정보 조회", description = "축제 ID에 해당하는 상세 행사 설명 및 일정을 조회합니다.")
    @GetMapping("/{festivalId}")
    public ApiResponse<FestivalDto> getFestival(
            @Parameter(description = "축제 고유 ID", example = "1")
            @PathVariable("festivalId") Long festivalId) {
        return ApiResponse.success("축제 상세 정보를 조회했습니다.", festivalService.getFestival(festivalId));
    }
}
