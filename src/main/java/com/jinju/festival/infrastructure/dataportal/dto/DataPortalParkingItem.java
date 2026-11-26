package com.jinju.festival.infrastructure.dataportal.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class DataPortalParkingItem {

    @JsonProperty("prkplceNo")
    @JsonAlias({"idx", "parkingId", "prkplceNo"})
    private String prkplceNo;

    @JsonProperty("prkplceNm")
    @JsonAlias({"parkingName", "prkplceNm", "name"})
    private String prkplceNm;

    @JsonProperty("totalPrkcm")
    @JsonAlias({"totalSpace", "totalPrkcm"})
    private Integer totalPrkcm;

    @JsonProperty("prkcm")
    @JsonAlias({"usedSpace", "prkcm"})
    private Integer prkcm; // 현재 주차 대수

    @JsonProperty("disPrkcm")
    @JsonAlias({"usedDisSpace", "disPrkcm"})
    private Integer disPrkcm; // 현재 장애인 주차 대수

    @JsonProperty("rdnmadr")
    private String rdnmadr; // 소재지 도로명주소

    @JsonProperty("lnmadr")
    private String lnmadr; // 소재지 지번주소

    @JsonProperty("address")
    @JsonAlias({"parkingAddress", "address"})
    private String address; // 일반 주소

    @JsonProperty("prkplceEntrc")
    @JsonAlias({"type", "prkplceEntrc"})
    private String prkplceEntrc; // 주차장 구획/유형

    public String getEffectiveAddress() {
        if (address != null && !address.isBlank()) {
            return address.trim();
        }
        if (rdnmadr != null && !rdnmadr.isBlank()) {
            return rdnmadr.trim();
        }
        if (lnmadr != null && !lnmadr.isBlank()) {
            return lnmadr.trim();
        }
        return null;
    }
}
