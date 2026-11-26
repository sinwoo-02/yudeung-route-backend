package com.jinju.festival.domain.zone.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiFrameDetailDto {

    @JsonProperty("zone_id")
    private Long zoneId;

    @JsonProperty("file_name")
    private String fileName;

    @JsonProperty("time_sec")
    private Double timeSec;

    @JsonProperty("timestamp")
    private String timestamp;

    @JsonProperty("frame_idx")
    private Integer frameIdx;

    @JsonProperty("person_count")
    private Integer personCount;

    @JsonProperty("raw_density_sum")
    private Double rawDensitySum;

    @JsonProperty("status")
    private String status;
}
