package com.jinju.festival.route;

import com.jinju.festival.domain.festival.repository.FestivalRepository;
import com.jinju.festival.domain.parking.entity.Parking;
import com.jinju.festival.domain.parking.repository.ParkingRepository;
import com.jinju.festival.domain.route.dto.RouteRecommendRequest;
import com.jinju.festival.domain.route.dto.RouteRecommendResponse;
import com.jinju.festival.domain.route.entity.RouteEdge;
import com.jinju.festival.domain.route.entity.RouteNode;
import com.jinju.festival.domain.route.repository.RouteEdgeRepository;
import com.jinju.festival.domain.route.repository.RouteNodeRepository;
import com.jinju.festival.domain.route.service.RouteRecommendationService;
import com.jinju.festival.domain.zone.repository.TouristZoneRepository;
import com.jinju.festival.global.util.AStarAlgorithm;
import com.jinju.festival.global.util.AStarAlgorithm.AStarResult;
import com.jinju.festival.global.util.AStarAlgorithm.GraphEdge;
import com.jinju.festival.global.util.GeoUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@SuppressWarnings("null")
class AStarAlgorithmTest {

    @Autowired
    private RouteRecommendationService routeRecommendationService;

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

    @BeforeEach
    void setUpRouteGraph() {
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
    @DisplayName("GeoUtil 거리 계산 및 최근접 노드 탐색 검증")
    void testGeoUtil() {
        // 진주성 촉석루 (35.1884, 128.0841) <-> 음악분수대 (35.1872, 128.0789)
        double distance = GeoUtil.calculateDistance(35.1884, 128.0841, 35.1872, 128.0789);
        assertThat(distance).isBetween(450.0, 650.0);

        RouteNode node1 = RouteNode.builder().nodeId(1L).latitude(35.1896).longitude(128.0807).nodeName("노드1").build();
        RouteNode node2 = RouteNode.builder().nodeId(2L).latitude(35.1831).longitude(128.0725).nodeName("노드2").build();

        // 35.1895, 128.0805와 가장 가까운 노드는 node1이어야 함
        RouteNode nearest = GeoUtil.findNearestNode(List.of(node1, node2), 35.1895, 128.0805);
        assertThat(nearest.getNodeId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("혼잡도 가중치에 따른 A* 알고리즘 우회 경로 탐색 검증")
    void testAStarCongestionDetour() {
        // Node 1 (Start) -> Node 2 (Short but Very Congested: 300m * 2.5 = 750) -> Node 4 (Goal)
        // Node 1 (Start) -> Node 3 (Longer but Smooth: 400m * 1.0 = 400) -> Node 4 (Goal: 400m * 1.0 = 400)
        RouteNode start = RouteNode.builder().nodeId(1L).latitude(35.1800).longitude(128.0800).nodeName("Start").build();
        RouteNode nodeCongested = RouteNode.builder().nodeId(2L).latitude(35.1820).longitude(128.0800).nodeName("Congested").build();
        RouteNode nodeDetour = RouteNode.builder().nodeId(3L).latitude(35.1800).longitude(128.0850).nodeName("Detour").build();
        RouteNode goal = RouteNode.builder().nodeId(4L).latitude(35.1840).longitude(128.0850).nodeName("Goal").build();

        Map<Long, RouteNode> nodeMap = Map.of(
                1L, start,
                2L, nodeCongested,
                3L, nodeDetour,
                4L, goal
        );

        Map<Long, List<GraphEdge>> adjacencyList = new HashMap<>();

        // 1 -> 2 (300m, 혼잡배수 2.5 -> 비용 750)
        adjacencyList.computeIfAbsent(1L, k -> new ArrayList<>()).add(
                GraphEdge.builder().targetNodeId(2L).baseDistanceMeters(300.0).congestionMultiplier(2.5).effectiveCost(750.0).build()
        );
        // 2 -> 4 (300m, 혼잡배수 2.5 -> 비용 750)
        adjacencyList.computeIfAbsent(2L, k -> new ArrayList<>()).add(
                GraphEdge.builder().targetNodeId(4L).baseDistanceMeters(300.0).congestionMultiplier(2.5).effectiveCost(750.0).build()
        );
        // 1 -> 3 (400m, 원활배수 1.0 -> 비용 400)
        adjacencyList.computeIfAbsent(1L, k -> new ArrayList<>()).add(
                GraphEdge.builder().targetNodeId(3L).baseDistanceMeters(400.0).congestionMultiplier(1.0).effectiveCost(400.0).build()
        );
        // 3 -> 4 (400m, 원활배수 1.0 -> 비용 400)
        adjacencyList.computeIfAbsent(3L, k -> new ArrayList<>()).add(
                GraphEdge.builder().targetNodeId(4L).baseDistanceMeters(400.0).congestionMultiplier(1.0).effectiveCost(400.0).build()
        );

        AStarResult result = AStarAlgorithm.findPath(start, goal, adjacencyList, nodeMap);

        assertThat(result.isFound()).isTrue();
        // 물리적 거리는 1->2->4(600m)가 짧지만, 혼잡도 가중비용(1500 vs 800)으로 인해 우회로(1->3->4) 선택
        List<Long> pathNodeIds = result.getPath().stream().map(RouteNode::getNodeId).toList();
        assertThat(pathNodeIds).containsExactly(1L, 3L, 4L);
        assertThat(result.getTotalDistanceMeters()).isEqualTo(800.0);
        assertThat(result.getTotalCost()).isEqualTo(800.0);
    }

    @Test
    @DisplayName("실제 유등축제 도로망 기반 RouteRecommendationService 최적 경로 추천 검증")
    void testRouteRecommendationService() {
        RouteRecommendRequest request = RouteRecommendRequest.builder()
                .originLatitude(35.1896)
                .originLongitude(128.0807) // 진주성 공영주차장 부근
                .destinationLatitude(35.1831)
                .destinationLongitude(128.0725) // 천수교 남단 부근
                .build();

        RouteRecommendResponse response = routeRecommendationService.recommendRoute(request);

        assertThat(response).isNotNull();
        assertThat(response.getPathNodes()).isNotEmpty();
        assertThat(response.getTotalDistanceMeters()).isGreaterThan(0.0);
        assertThat(response.getEstimatedWalkingTimeMinutes()).isGreaterThan(0);
        assertThat(response.getEstimatedDrivingTimeMinutes()).isGreaterThan(0);
        assertThat(response.getCongestionSummary()).isNotBlank();
    }

    @Test
    @DisplayName("목적지 주차장 지정 방식 경로 추천 검증")
    void testRouteToParking() {
        var festival = festivalRepository.findAll().get(0);
        var parking = parkingRepository.save(Parking.builder()
                .festival(festival)
                .name("망경동 남강둔치 주차장")
                .address("경남 진주시 망경동 199-1")
                .latitude(new BigDecimal("35.1831"))
                .longitude(new BigDecimal("128.0725"))
                .totalSpace(300)
                .build());

        RouteRecommendRequest request = RouteRecommendRequest.builder()
                .originLatitude(35.1896)
                .originLongitude(128.0807)
                .destinationParkingId(parking.getParkingId())
                .build();

        RouteRecommendResponse response = routeRecommendationService.recommendRoute(request);

        assertThat(response).isNotNull();
        assertThat(response.getDestination().getDescription()).contains("망경동 남강둔치 주차장");
        assertThat(response.getPathNodes()).isNotEmpty();
    }
}
