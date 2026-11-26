package com.jinju.festival.infrastructure.kakao.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class KakaoDirectionsResponse {

    @JsonProperty("routes")
    private List<Route> routes;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Route {
        @JsonProperty("result_code")
        private Integer resultCode;

        @JsonProperty("result_msg")
        private String resultMsg;

        @JsonProperty("summary")
        private Summary summary;

        @JsonProperty("sections")
        private List<Section> sections;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Summary {
        @JsonProperty("distance")
        private Integer distance; // 미터(m)

        @JsonProperty("duration")
        private Integer duration; // 초(s)
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Section {
        @JsonProperty("distance")
        private Integer distance;

        @JsonProperty("duration")
        private Integer duration;

        @JsonProperty("roads")
        private List<Road> roads;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Road {
        @JsonProperty("name")
        private String name;

        @JsonProperty("distance")
        private Integer distance;

        @JsonProperty("duration")
        private Integer duration;

        @JsonProperty("vertexes")
        private List<Double> vertexes; // [lon, lat, lon, lat, ...]
    }

    public List<Route> getRoutes() {
        return routes != null ? routes : Collections.emptyList();
    }
}
