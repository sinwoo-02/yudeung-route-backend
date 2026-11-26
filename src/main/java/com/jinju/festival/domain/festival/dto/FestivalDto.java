package com.jinju.festival.domain.festival.dto;

import com.jinju.festival.domain.festival.entity.Festival;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FestivalDto {

    private Long festivalId;
    private String name;
    private LocalDate startDate;
    private LocalDate endDate;
    private String location;
    private String description;
    private LocalDateTime createdAt;

    public static FestivalDto from(Festival festival) {
        if (festival == null) return null;
        return FestivalDto.builder()
                .festivalId(festival.getFestivalId())
                .name(festival.getName())
                .startDate(festival.getStartDate())
                .endDate(festival.getEndDate())
                .location(festival.getLocation())
                .description(festival.getDescription())
                .createdAt(festival.getCreatedAt())
                .build();
    }
}
