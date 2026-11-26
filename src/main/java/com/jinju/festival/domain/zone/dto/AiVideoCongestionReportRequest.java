package com.jinju.festival.domain.zone.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiVideoCongestionReportRequest {

    @JsonProperty("zone_id")
    private Long zoneId;

    @JsonProperty("file_name")
    private String fileName;

    @JsonProperty("video_path")
    private String videoPath;

    @JsonProperty("total_duration_sec")
    private Double totalDurationSec;

    @JsonProperty("total_frames_analyzed")
    private Integer totalFramesAnalyzed;

    @JsonProperty("max_person_count")
    private Integer maxPersonCount;

    @JsonProperty("avg_person_count")
    private Double avgPersonCount;

    @JsonProperty("overall_status")
    private String overallStatus;

    @JsonProperty("frames")
    private List<AiFrameDetailDto> frames;

    /**
     * 최상위에 zone_id가 없으면 frames의 첫 번째 요소의 zone_id를 fallback으로 반환
     */
    public Long getResolvedZoneId() {
        if (zoneId != null) {
            return zoneId;
        }
        if (frames != null && !frames.isEmpty()) {
            for (AiFrameDetailDto frame : frames) {
                if (frame.getZoneId() != null) {
                    return frame.getZoneId();
                }
            }
        }
        return null;
    }

    /**
     * 영상의 대표 인원수 (avg_person_count 반올림 -> max_person_count -> frames 첫 번째)
     */
    public Integer getResolvedPeopleCount() {
        if (avgPersonCount != null) {
            return (int) Math.round(avgPersonCount);
        }
        if (maxPersonCount != null) {
            return maxPersonCount;
        }
        if (frames != null && !frames.isEmpty()) {
            for (AiFrameDetailDto frame : frames) {
                if (frame.getPersonCount() != null) {
                    return frame.getPersonCount();
                }
            }
        }
        return 0;
    }

    /**
     * 영상의 대표 혼잡도 상태
     */
    public String getResolvedOverallStatus() {
        if (overallStatus != null && !overallStatus.isBlank()) {
            return overallStatus;
        }
        if (frames != null && !frames.isEmpty()) {
            for (AiFrameDetailDto frame : frames) {
                if (frame.getStatus() != null && !frame.getStatus().isBlank()) {
                    return frame.getStatus();
                }
            }
        }
        return "NORMAL";
    }
}
