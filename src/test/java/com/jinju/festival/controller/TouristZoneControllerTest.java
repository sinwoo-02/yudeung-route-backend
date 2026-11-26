package com.jinju.festival.controller;

import com.jinju.festival.domain.zone.entity.TouristZone;
import com.jinju.festival.domain.zone.repository.TouristZoneRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class TouristZoneControllerTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private TouristZoneRepository touristZoneRepository;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    @DisplayName("전체 관광 구역 목록 및 실시간 혼잡도 조회 API 검증")
    void testGetAllZones() throws Exception {
        mockMvc.perform(get("/api/v1/zones")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.data[0].zoneId").exists())
                .andExpect(jsonPath("$.data[0].zoneName").isString())
                .andExpect(jsonPath("$.data[0].peopleCount").isNumber())
                .andExpect(jsonPath("$.data[0].congestionLevel").isString());
    }

    @Test
    @DisplayName("특정 관광 구역 상세 및 최근 혼잡도 이력 조회 API 검증")
    void testGetZoneDetail() throws Exception {
        TouristZone zone = touristZoneRepository.findAll().get(0);

        mockMvc.perform(get("/api/v1/zones/" + zone.getZoneId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.zone.zoneId").value(zone.getZoneId()))
                .andExpect(jsonPath("$.data.zone.zoneName").value(zone.getZoneName()))
                .andExpect(jsonPath("$.data.recentCongestionHistories").isArray());
    }

    @Test
    @DisplayName("존재하지 않는 관광 구역 조회 시 404 에러 반환 검증")
    void testGetZoneDetailNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/zones/999999")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("AI 서버 CCTV 혼잡도 데이터 수신 및 저장 API 검증 (배열 형태)")
    void testReceiveAiCongestionReports_Array() throws Exception {
        TouristZone zone = touristZoneRepository.findAll().get(0);

        String jsonPayload = """
                [
                  {
                    "zone_id": %d,
                    "file_name": "Hailuo_Video_test.mp4",
                    "video_path": "videos\\\\Hailuo_Video_test.mp4",
                    "total_duration_sec": 10.12,
                    "total_frames_analyzed": 11,
                    "max_person_count": 50,
                    "avg_person_count": 44.45,
                    "overall_status": "NORMAL",
                    "frames": [
                      {
                        "zone_id": %d,
                        "file_name": "Hailuo_Video_test.mp4",
                        "time_sec": 0.0,
                        "timestamp": "00:00",
                        "frame_idx": 0,
                        "person_count": 46,
                        "raw_density_sum": 46.45,
                        "status": "NORMAL"
                      }
                    ]
                  }
                ]
                """.formatted(zone.getZoneId(), zone.getZoneId());

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/v1/zones/congestion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.savedCount").value(1))
                .andExpect(jsonPath("$.data.savedHistories[0].peopleCount").value(44))
                .andExpect(jsonPath("$.data.savedHistories[0].congestionLevel").value("원활"));
    }

    @Test
    @DisplayName("AI 서버 CCTV 혼잡도 데이터 수신 및 저장 API 검증 (단일 객체 형태)")
    void testReceiveAiCongestionReports_Single() throws Exception {
        TouristZone zone = touristZoneRepository.findAll().get(0);

        String jsonPayload = """
                {
                  "zone_id": %d,
                  "file_name": "single_cctv.mp4",
                  "avg_person_count": 85.0,
                  "overall_status": "CONGESTED"
                }
                """.formatted(zone.getZoneId());

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/v1/zones/congestion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.savedCount").value(1))
                .andExpect(jsonPath("$.data.savedHistories[0].peopleCount").value(85))
                .andExpect(jsonPath("$.data.savedHistories[0].congestionLevel").value("혼잡"));
    }
}
