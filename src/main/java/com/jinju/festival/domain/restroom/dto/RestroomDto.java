package com.jinju.festival.domain.restroom.dto;

import com.jinju.festival.domain.restroom.entity.Restroom;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RestroomDto {

    private Long restroomId;
    private Long festivalId;
    private String name;
    private String restroomType;
    private BigDecimal latitude;
    private BigDecimal longitude;

    public static RestroomDto from(Restroom restroom) {
        if (restroom == null) {
            return null;
        }
        Long festivalId = (restroom.getFestival() != null) ? restroom.getFestival().getFestivalId() : null;

        return RestroomDto.builder()
                .restroomId(restroom.getRestroomId())
                .festivalId(festivalId)
                .name(restroom.getName())
                .restroomType(restroom.getRestroomType())
                .latitude(restroom.getLatitude())
                .longitude(restroom.getLongitude())
                .build();
    }
}

