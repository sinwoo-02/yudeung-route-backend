package com.jinju.festival.infrastructure.dataportal.dto;

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
public class DataPortalParkingResponse {

    @JsonProperty("response")
    private ResponseDetail response;

    @JsonProperty("header")
    private Header header;

    @JsonProperty("body")
    private Body body;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ResponseDetail {
        @JsonProperty("header")
        private Header header;

        @JsonProperty("body")
        private Body body;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Header {
        @JsonProperty("resultCode")
        private String resultCode;

        @JsonProperty("resultMsg")
        private String resultMsg;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Body {
        @JsonProperty("items")
        private List<DataPortalParkingItem> items;

        @JsonProperty("totalCount")
        private Integer totalCount;

        @JsonProperty("pageNo")
        private Integer pageNo;

        @JsonProperty("numOfRows")
        private Integer numOfRows;
    }

    public List<DataPortalParkingItem> getItems() {
        if (body != null && body.getItems() != null) {
            return body.getItems();
        }
        if (response != null && response.getBody() != null && response.getBody().getItems() != null) {
            return response.getBody().getItems();
        }
        return Collections.emptyList();
    }
}
