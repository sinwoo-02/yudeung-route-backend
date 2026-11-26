package com.jinju.festival.controller;

import com.jinju.festival.domain.parking.entity.Parking;
import com.jinju.festival.domain.parking.entity.ParkingStatusHistory;
import com.jinju.festival.domain.parking.repository.ParkingRepository;
import com.jinju.festival.domain.parking.repository.ParkingStatusHistoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
class ParkingControllerTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private ParkingRepository parkingRepository;

    @Autowired
    private ParkingStatusHistoryRepository parkingStatusHistoryRepository;

    private MockMvc mockMvc;
    private Parking testParking;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();

        testParking = parkingRepository.save(
                Parking.builder()
                        .name("테스트 공영주차장")
                        .address("진주시 남강로 1")
                        .parkingType("공영주차장")
                        .latitude(new BigDecimal("35.1884"))
                        .longitude(new BigDecimal("128.0841"))
                        .totalSpace(100)
                        .disSpace(5)
                        .build()
        );

        parkingStatusHistoryRepository.save(
                ParkingStatusHistory.builder()
                        .parking(testParking)
                        .currentParkingCount(40)
                        .usedSpace(40)
                        .usedDisSpace(1)
                        .availableSpaces(60)
                        .occupancyRate(new BigDecimal("40.00"))
                        .isFull(false)
                        .congestionStatus("원활")
                        .build()
        );
    }

    @Test
    @DisplayName("전체 주차장 목록 및 실시간 상태 조회 API 검증")
    void testGetAllParkings() throws Exception {
        mockMvc.perform(get("/api/v1/parkings")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.data[0].parkingId").isNumber())
                .andExpect(jsonPath("$.data[0].availableSpaces").isNumber())
                .andExpect(jsonPath("$.data[0].congestionStatus").isString());
    }

    @Test
    @DisplayName("특정 주차장 상세 및 최근 이력 조회 API 검증")
    void testGetParkingDetail() throws Exception {
        mockMvc.perform(get("/api/v1/parkings/" + testParking.getParkingId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.parking.parkingId").value(testParking.getParkingId()))
                .andExpect(jsonPath("$.data.parking.name").value("테스트 공영주차장"))
                .andExpect(jsonPath("$.data.recentStatusHistories").isArray());
    }

    @Test
    @DisplayName("존재하지 않는 주차장 조회 시 404 에러 반환 검증")
    void testGetParkingDetailNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/parkings/999999")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").exists());
    }
}
