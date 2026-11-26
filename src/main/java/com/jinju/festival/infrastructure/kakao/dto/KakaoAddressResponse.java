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
public class KakaoAddressResponse {

    @JsonProperty("meta")
    private Meta meta;

    @JsonProperty("documents")
    private List<Document> documents;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Meta {
        @JsonProperty("total_count")
        private Integer totalCount;

        @JsonProperty("is_end")
        private Boolean isEnd;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Document {
        @JsonProperty("address_name")
        private String addressName;

        @JsonProperty("x")
        private String x; // 경도 (Longitude)

        @JsonProperty("y")
        private String y; // 위도 (Latitude)
    }

    public List<Document> getDocuments() {
        return documents != null ? documents : Collections.emptyList();
    }
}
