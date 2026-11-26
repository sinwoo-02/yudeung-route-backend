package com.jinju.festival.infrastructure.kakao.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KakaoCoordinate {
    private Double latitude;
    private Double longitude;
    private String name;
    private String address;
}
