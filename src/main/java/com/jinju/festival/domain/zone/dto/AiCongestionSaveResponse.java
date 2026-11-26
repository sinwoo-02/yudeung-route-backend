package com.jinju.festival.domain.zone.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiCongestionSaveResponse {

    private int savedCount;
    private List<ZoneCongestionHistoryDto> savedHistories;
}
