package com.jinju.festival.domain.restroom.controller;

import com.jinju.festival.domain.restroom.dto.RestroomDto;
import com.jinju.festival.domain.restroom.service.RestroomService;
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

@Tag(name = "5. 화장실 위치 안내 API", description = "축제장 내/인근 화장실 이름 및 위경도 위치 좌표 조회")
@RestController
@RequestMapping("/api/v1/restrooms")
@RequiredArgsConstructor
public class RestroomController {

    private final RestroomService restroomService;

    @Operation(
            summary = "전체 화장실 위치 및 이름 목록 조회",
            description = "프론트엔드 지도 마커 표시를 위해 축제장 내 모든 화장실의 이름, 유형, 위도(latitude), 경도(longitude) 목록을 반환합니다."
    )
    @GetMapping
    public ApiResponse<List<RestroomDto>> getAllRestrooms() {
        return ApiResponse.success("전체 화장실 위치 목록을 조회했습니다.", restroomService.getAllRestrooms());
    }

    @Operation(
            summary = "특정 화장실 상세 조회",
            description = "화장실 고유 ID에 해당하는 화장실 이름 및 위치 좌표를 조회합니다."
    )
    @GetMapping("/{restroomId}")
    public ApiResponse<RestroomDto> getRestroom(
            @Parameter(description = "화장실 고유 ID", example = "1")
            @PathVariable("restroomId") Long restroomId) {
        return ApiResponse.success("화장실 상세 정보를 조회했습니다.", restroomService.getRestroom(restroomId));
    }
}

