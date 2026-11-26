package com.jinju.festival.controller;

import com.jinju.festival.domain.festival.repository.FestivalRepository;
import com.jinju.festival.domain.parking.entity.Parking;
import com.jinju.festival.domain.parking.repository.ParkingRepository;
import com.jinju.festival.domain.route.entity.RouteEdge;
import com.jinju.festival.domain.route.entity.RouteNode;
import com.jinju.festival.domain.route.repository.RouteEdgeRepository;
import com.jinju.festival.domain.route.repository.RouteNodeRepository;
import com.jinju.festival.domain.zone.repository.TouristZoneRepository;
import com.jinju.festival.global.util.GeoUtil;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
@SuppressWarnings("null")
class RouteControllerTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private ParkingRepository parkingRepository;

    @Autowired
    private FestivalRepository festivalRepository;

    @Autowired
    private TouristZoneRepository touristZoneRepository;

    @Autowired
    private RouteNodeRepository routeNodeRepository;

    @Autowired
    private RouteEdgeRepository routeEdgeRepository;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();

        if (routeEdgeRepository.count() == 0) {
            var allNodes = routeNodeRepository.findAll();
            if (allNodes.isEmpty()) {
                var zones = touristZoneRepository.findAll();
                var n1 = routeNodeRepository.save(RouteNode.builder()
                        .touristZone(zones.get(0)).latitude(35.1896).longitude(128.0807).nodeName("테스트 시작 노드").build());
                var n2 = routeNodeRepository.save(RouteNode.builder()
                        .touristZone(zones.get(1)).latitude(35.1831).longitude(128.0725).nodeName("테스트 도착 노드").build());
                routeEdgeRepository.save(RouteEdge.builder()
                        .startNode(n1).endNode(n2).baseDistanceMeter(500.0).isBidirectional(true).build());
            } else {
                RouteNode nearestStart = GeoUtil.findNearestNode(allNodes, 35.1896, 128.0807);
                RouteNode nearestGoal = GeoUtil.findNearestNode(allNodes, 35.1831, 128.0725);
                routeEdgeRepository.save(RouteEdge.builder()
                        .startNode(nearestStart).endNode(nearestGoal).baseDistanceMeter(800.0).isBidirectional(true).build());
            }
        }
    }

    @Test
    @DisplayName("CCTV 혼잡도 기반 A* 최적 경로 추천 API (POST) 검증")
    void testRecommendRoutePost() throws Exception {
        String requestJson = """
                {
                    "originLatitude": 35.1896,
                    "originLongitude": 128.0807,
                    "destinationLatitude": 35.1831,
                    "destinationLongitude": 128.0725
                }
                """;

        mockMvc.perform(post("/api/v1/routes/recommend")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.startNode").exists())
                .andExpect(jsonPath("$.data.goalNode").exists())
                .andExpect(jsonPath("$.data.pathNodes", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.data.totalDistanceMeters").isNumber())
                .andExpect(jsonPath("$.data.totalWeightedCost").isNumber())
                .andExpect(jsonPath("$.data.estimatedWalkingTimeMinutes").isNumber())
                .andExpect(jsonPath("$.data.estimatedDrivingTimeMinutes").isNumber())
                .andExpect(jsonPath("$.data.congestionSummary").isString());
    }

    @Test
    @DisplayName("CCTV 혼잡도 기반 A* 최적 경로 추천 API (GET 쿼리 파라미터) 검증")
    void testRecommendRouteGet() throws Exception {
        var festival = festivalRepository.findAll().get(0);
        var parking = parkingRepository.save(Parking.builder()
                .festival(festival)
                .name("망경동 남강둔치 주차장")
                .address("경남 진주시 망경동 199-1")
                .latitude(new BigDecimal("35.1831"))
                .longitude(new BigDecimal("128.0725"))
                .totalSpace(300)
                .build());

        mockMvc.perform(get("/api/v1/routes/recommend")
                        .param("originLat", "35.1896")
                        .param("originLon", "128.0807")
                        .param("parkingId", parking.getParkingId().toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.pathNodes", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.data.totalDistanceMeters").isNumber());
    }

    @Test
    @DisplayName("전체 도로망 노드 목록 조회 API 검증")
    void testGetAllNodes() throws Exception {
        mockMvc.perform(get("/api/v1/routes/nodes")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.data[0].nodeId").exists())
                .andExpect(jsonPath("$.data[0].nodeName").isString());
    }
}
