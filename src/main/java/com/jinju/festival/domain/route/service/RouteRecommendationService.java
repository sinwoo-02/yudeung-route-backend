package com.jinju.festival.domain.route.service;

import com.jinju.festival.domain.parking.entity.Parking;
import com.jinju.festival.domain.parking.repository.ParkingRepository;
import com.jinju.festival.domain.route.dto.RouteNodeDto;
import com.jinju.festival.domain.route.dto.RouteRecommendRequest;
import com.jinju.festival.domain.route.dto.RouteRecommendResponse;
import com.jinju.festival.domain.route.entity.RouteEdge;
import com.jinju.festival.domain.route.entity.RouteNode;
import com.jinju.festival.domain.route.repository.RouteEdgeRepository;
import com.jinju.festival.domain.route.repository.RouteNodeRepository;
import com.jinju.festival.domain.zone.entity.TouristZone;
import com.jinju.festival.domain.zone.entity.ZoneCongestionHistory;
import com.jinju.festival.domain.zone.repository.TouristZoneRepository;
import com.jinju.festival.domain.zone.repository.ZoneCongestionHistoryRepository;
import com.jinju.festival.global.util.AStarAlgorithm;
import com.jinju.festival.global.util.AStarAlgorithm.AStarResult;
import com.jinju.festival.global.util.AStarAlgorithm.GraphEdge;
import com.jinju.festival.global.util.GeoUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RouteRecommendationService {

    private final RouteNodeRepository routeNodeRepository;
    private final RouteEdgeRepository routeEdgeRepository;
    private final ZoneCongestionHistoryRepository zoneCongestionHistoryRepository;
    private final TouristZoneRepository touristZoneRepository;
    private final ParkingRepository parkingRepository;

    /**
     * 도로망 전체 노드 목록 조회 (소속 구역 및 실시간 혼잡도 포함)
     */
    public List<RouteNodeDto> getAllRouteNodes() {
        Map<Long, ZoneCongestionInfo> zoneCongestionMap = buildZoneCongestionMap();
        return routeNodeRepository.findAllWithTouristZone().stream()
                .map(node -> {
                    ZoneCongestionInfo info = getCongestionInfoForNode(node, zoneCongestionMap);
                    return RouteNodeDto.from(node, info.level, info.multiplier);
                })
                .toList();
    }

    /**
     * CCTV 혼잡도 가중치와 A* 알고리즘을 이용한 최적 경로 추천
     */
    public RouteRecommendResponse recommendRoute(RouteRecommendRequest request) {
        List<RouteNode> allNodes = routeNodeRepository.findAllWithTouristZone();
        if (allNodes.isEmpty()) {
            throw new IllegalStateException("등록된 도로망 노드가 없어 경로를 탐색할 수 없습니다.");
        }

        Map<Long, RouteNode> nodeMap = allNodes.stream()
                .collect(Collectors.toMap(RouteNode::getNodeId, Function.identity()));

        // 1. 구역별 최신 혼잡도 및 가중 배수 매핑
        Map<Long, ZoneCongestionInfo> zoneCongestionMap = buildZoneCongestionMap();

        // 2. A* 그래프 인접 리스트 구축
        List<RouteEdge> allEdges = routeEdgeRepository.findAllWithNodes();
        Map<Long, List<GraphEdge>> adjacencyList = buildAdjacencyList(allEdges, nodeMap, zoneCongestionMap);

        // 3. 출발지 및 목적지 노드 매핑
        ResolvedEndpoints endpoints = resolveEndpoints(request, allNodes);
        RouteNode startNode = endpoints.startNode;
        RouteNode goalNode = endpoints.goalNode;

        // 4. A* 알고리즘 수행
        AStarResult aStarResult = AStarAlgorithm.findPath(startNode, goalNode, adjacencyList, nodeMap);

        if (!aStarResult.isFound()) {
            throw new IllegalStateException("출발지와 목적지 간 연결된 경로를 찾을 수 없습니다.");
        }

        // 5. 결과 DTO 구성
        List<RouteNodeDto> pathNodeDtos = aStarResult.getPath().stream()
                .map(node -> {
                    ZoneCongestionInfo info = getCongestionInfoForNode(node, zoneCongestionMap);
                    return RouteNodeDto.from(node, info.level, info.multiplier);
                })
                .toList();

        double totalDistance = aStarResult.getTotalDistanceMeters();
        double totalCost = aStarResult.getTotalCost();

        // 도보 평균 속도: 약 70m/분 (4.2km/h)
        int estimatedWalkingTimeMinutes = (int) Math.max(1, Math.ceil(totalDistance / 70.0));
        // 차량 이동 속도 (혼잡도 가중치 반영): 혼잡 시 지연 반영
        int estimatedDrivingTimeMinutes = (int) Math.max(1, Math.ceil(totalCost / 400.0));

        String congestionSummary = generateCongestionSummary(pathNodeDtos, totalDistance, totalCost);

        ZoneCongestionInfo startInfo = getCongestionInfoForNode(startNode, zoneCongestionMap);
        ZoneCongestionInfo goalInfo = getCongestionInfoForNode(goalNode, zoneCongestionMap);

        return RouteRecommendResponse.builder()
                .origin(endpoints.originPoint)
                .destination(endpoints.destinationPoint)
                .startNode(RouteNodeDto.from(startNode, startInfo.level, startInfo.multiplier))
                .goalNode(RouteNodeDto.from(goalNode, goalInfo.level, goalInfo.multiplier))
                .pathNodes(pathNodeDtos)
                .totalDistanceMeters(totalDistance)
                .totalWeightedCost(totalCost)
                .estimatedWalkingTimeMinutes(estimatedWalkingTimeMinutes)
                .estimatedDrivingTimeMinutes(estimatedDrivingTimeMinutes)
                .congestionSummary(congestionSummary)
                .build();
    }

    private Map<Long, ZoneCongestionInfo> buildZoneCongestionMap() {
        Map<Long, ZoneCongestionInfo> map = new HashMap<>();
        List<ZoneCongestionHistory> latestCongestions = zoneCongestionHistoryRepository.findLatestCongestionForAllZones();

        for (ZoneCongestionHistory history : latestCongestions) {
            if (history.getTouristZone() != null) {
                Long zoneId = history.getTouristZone().getZoneId();
                String level = history.getCongestionLevel();
                double multiplier = getCongestionMultiplier(level);
                map.put(zoneId, new ZoneCongestionInfo(level, multiplier));
            }
        }
        return map;
    }

    public static double getCongestionMultiplier(String level) {
        if (level == null) return 1.0;
        return switch (level.trim()) {
            case "매우 혼잡", "만차" -> 2.5;
            case "혼잡" -> 1.8;
            case "보통" -> 1.3;
            case "원활" -> 1.0;
            default -> 1.0;
        };
    }

    private Map<Long, List<GraphEdge>> buildAdjacencyList(List<RouteEdge> edges,
                                                          Map<Long, RouteNode> nodeMap,
                                                          Map<Long, ZoneCongestionInfo> zoneCongestionMap) {
        Map<Long, List<GraphEdge>> adjacencyList = new HashMap<>();

        for (RouteEdge edge : edges) {
            if (edge.getStartNode() == null || edge.getEndNode() == null) {
                continue;
            }

            Long startId = edge.getStartNode().getNodeId();
            Long endId = edge.getEndNode().getNodeId();
            double baseDistance = (edge.getBaseDistanceMeter() != null) ? edge.getBaseDistanceMeter() : 100.0;

            RouteNode endNode = nodeMap.get(endId);
            ZoneCongestionInfo endInfo = getCongestionInfoForNode(endNode, zoneCongestionMap);
            double forwardCost = baseDistance * endInfo.multiplier;

            adjacencyList.computeIfAbsent(startId, k -> new ArrayList<>()).add(
                    GraphEdge.builder()
                            .targetNodeId(endId)
                            .baseDistanceMeters(baseDistance)
                            .congestionMultiplier(endInfo.multiplier)
                            .effectiveCost(forwardCost)
                            .build()
            );

            // 양방향 도로인 경우 역방향 간선 추가
            if (Boolean.TRUE.equals(edge.getIsBidirectional())) {
                RouteNode startNode = nodeMap.get(startId);
                ZoneCongestionInfo startInfo = getCongestionInfoForNode(startNode, zoneCongestionMap);
                double backwardCost = baseDistance * startInfo.multiplier;

                adjacencyList.computeIfAbsent(endId, k -> new ArrayList<>()).add(
                        GraphEdge.builder()
                                .targetNodeId(startId)
                                .baseDistanceMeters(baseDistance)
                                .congestionMultiplier(startInfo.multiplier)
                                .effectiveCost(backwardCost)
                                .build()
                );
            }
        }

        return adjacencyList;
    }

    private ZoneCongestionInfo getCongestionInfoForNode(RouteNode node, Map<Long, ZoneCongestionInfo> map) {
        if (node != null && node.getTouristZone() != null) {
            Long zoneId = node.getTouristZone().getZoneId();
            if (map.containsKey(zoneId)) {
                return map.get(zoneId);
            }
        }
        return new ZoneCongestionInfo("원활", 1.0);
    }

    private ResolvedEndpoints resolveEndpoints(RouteRecommendRequest request, List<RouteNode> allNodes) {
        RouteNode startNode;
        RouteRecommendResponse.LocationPoint originPoint;

        // 시작점 결정
        if (request.getStartNodeId() != null) {
            startNode = allNodes.stream()
                    .filter(n -> n.getNodeId().equals(request.getStartNodeId()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("지정된 시작 노드를 찾을 수 없습니다: " + request.getStartNodeId()));
            originPoint = RouteRecommendResponse.LocationPoint.builder()
                    .latitude(startNode.getLatitude())
                    .longitude(startNode.getLongitude())
                    .description(startNode.getNodeName())
                    .build();
        } else if (request.getOriginLatitude() != null && request.getOriginLongitude() != null) {
            startNode = GeoUtil.findNearestNode(allNodes, request.getOriginLatitude(), request.getOriginLongitude());
            originPoint = RouteRecommendResponse.LocationPoint.builder()
                    .latitude(request.getOriginLatitude())
                    .longitude(request.getOriginLongitude())
                    .description("사용자 지정 출발지")
                    .build();
        } else {
            // 기본값: 첫 번째 노드
            startNode = allNodes.get(0);
            originPoint = RouteRecommendResponse.LocationPoint.builder()
                    .latitude(startNode.getLatitude())
                    .longitude(startNode.getLongitude())
                    .description(startNode.getNodeName())
                    .build();
        }

        // 목적지 결정
        RouteNode goalNode;
        RouteRecommendResponse.LocationPoint destinationPoint;

        if (request.getGoalNodeId() != null) {
            goalNode = allNodes.stream()
                    .filter(n -> n.getNodeId().equals(request.getGoalNodeId()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("지정된 목적지 노드를 찾을 수 없습니다: " + request.getGoalNodeId()));
            destinationPoint = RouteRecommendResponse.LocationPoint.builder()
                    .latitude(goalNode.getLatitude())
                    .longitude(goalNode.getLongitude())
                    .description(goalNode.getNodeName())
                    .build();
        } else if (request.getDestinationParkingId() != null) {
            Parking parking = parkingRepository.findById(request.getDestinationParkingId())
                    .orElseThrow(() -> new IllegalArgumentException("지정된 주차장을 찾을 수 없습니다: " + request.getDestinationParkingId()));
            double lat = parking.getLatitude().doubleValue();
            double lon = parking.getLongitude().doubleValue();
            goalNode = GeoUtil.findNearestNode(allNodes, lat, lon);
            destinationPoint = RouteRecommendResponse.LocationPoint.builder()
                    .latitude(lat)
                    .longitude(lon)
                    .description("목적 주차장: " + parking.getName())
                    .build();
        } else if (request.getDestinationZoneId() != null) {
            TouristZone zone = touristZoneRepository.findById(request.getDestinationZoneId())
                    .orElseThrow(() -> new IllegalArgumentException("지정된 구역을 찾을 수 없습니다: " + request.getDestinationZoneId()));
            double lat = zone.getLatitude().doubleValue();
            double lon = zone.getLongitude().doubleValue();
            goalNode = GeoUtil.findNearestNode(allNodes, lat, lon);
            destinationPoint = RouteRecommendResponse.LocationPoint.builder()
                    .latitude(lat)
                    .longitude(lon)
                    .description("목적 관광구역: " + zone.getZoneName())
                    .build();
        } else if (request.getDestinationLatitude() != null && request.getDestinationLongitude() != null) {
            goalNode = GeoUtil.findNearestNode(allNodes, request.getDestinationLatitude(), request.getDestinationLongitude());
            destinationPoint = RouteRecommendResponse.LocationPoint.builder()
                    .latitude(request.getDestinationLatitude())
                    .longitude(request.getDestinationLongitude())
                    .description("사용자 지정 목적지")
                    .build();
        } else {
            // 기본값: 마지막 노드
            goalNode = allNodes.get(allNodes.size() - 1);
            destinationPoint = RouteRecommendResponse.LocationPoint.builder()
                    .latitude(goalNode.getLatitude())
                    .longitude(goalNode.getLongitude())
                    .description(goalNode.getNodeName())
                    .build();
        }

        return new ResolvedEndpoints(startNode, goalNode, originPoint, destinationPoint);
    }

    private String generateCongestionSummary(List<RouteNodeDto> path, double distance, double cost) {
        List<String> congestedZones = path.stream()
                .filter(node -> node.getCongestionMultiplier() > 1.3)
                .map(node -> node.getZoneName() + "(" + node.getCongestionLevel() + ")")
                .distinct()
                .toList();

        if (congestedZones.isEmpty()) {
            return String.format("추천 경로는 전체 구간이 원활/보통 수준으로 혼잡 지연 없이 이동하실 수 있습니다. (총 %.0fm)", distance);
        } else {
            return String.format("CCTV 분석 결과 혼잡 구역[%s]의 가중치가 반영되어 우회 및 최적화된 경로입니다. (총 %.0fm)",
                    String.join(", ", congestedZones), distance);
        }
    }

    private record ZoneCongestionInfo(String level, double multiplier) {}
    private record ResolvedEndpoints(RouteNode startNode,
                                     RouteNode goalNode,
                                     RouteRecommendResponse.LocationPoint originPoint,
                                     RouteRecommendResponse.LocationPoint destinationPoint) {}
}
