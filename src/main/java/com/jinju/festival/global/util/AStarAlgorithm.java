package com.jinju.festival.global.util;

import com.jinju.festival.domain.route.entity.RouteNode;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

@Slf4j
public class AStarAlgorithm {

    @Getter
    @Builder
    public static class GraphEdge {
        private final Long targetNodeId;
        private final double baseDistanceMeters;
        private final double congestionMultiplier;
        private final double effectiveCost; // baseDistanceMeters * congestionMultiplier
    }

    @Getter
    @Builder
    public static class AStarResult {
        private final List<RouteNode> path;
        private final double totalDistanceMeters;
        private final double totalCost;
        private final boolean found;
    }

    private static class NodeRecord implements Comparable<NodeRecord> {
        final Long nodeId;
        final double gCost;
        final double hCost;
        final double fCost;

        NodeRecord(Long nodeId, double gCost, double hCost) {
            this.nodeId = nodeId;
            this.gCost = gCost;
            this.hCost = hCost;
            this.fCost = gCost + hCost;
        }

        @Override
        public int compareTo(NodeRecord other) {
            return Double.compare(this.fCost, other.fCost);
        }
    }

    /**
     * A* (A-Star) 알고리즘을 이용한 최적 경로 탐색
     *
     * @param start 시작 노드
     * @param goal 목적지 노드
     * @param adjacencyList 그래프 인접 리스트 (nodeId -> List of GraphEdge)
     * @param nodeMap 노드 ID -> RouteNode 엔티티 맵
     * @return 최적 경로 및 소요 거리/비용 결과를 담은 AStarResult
     */
    public static AStarResult findPath(RouteNode start,
                                      RouteNode goal,
                                      Map<Long, List<GraphEdge>> adjacencyList,
                                      Map<Long, RouteNode> nodeMap) {
        if (start == null || goal == null) {
            throw new IllegalArgumentException("시작 노드와 목적지 노드는 null일 수 없습니다.");
        }

        Long startId = start.getNodeId();
        Long goalId = goal.getNodeId();

        if (startId.equals(goalId)) {
            return AStarResult.builder()
                    .path(List.of(start))
                    .totalDistanceMeters(0.0)
                    .totalCost(0.0)
                    .found(true)
                    .build();
        }

        PriorityQueue<NodeRecord> openSet = new PriorityQueue<>();
        Map<Long, Double> gScores = new HashMap<>();
        Map<Long, Long> cameFrom = new HashMap<>();
        Map<Long, GraphEdge> cameFromEdge = new HashMap<>();
        Set<Long> closedSet = new HashSet<>();

        double startH = calculateHeuristic(start, goal);
        gScores.put(startId, 0.0);
        openSet.add(new NodeRecord(startId, 0.0, startH));

        while (!openSet.isEmpty()) {
            NodeRecord current = openSet.poll();
            Long currentId = current.nodeId;

            if (currentId.equals(goalId)) {
                return reconstructPath(startId, goalId, cameFrom, cameFromEdge, nodeMap);
            }

            if (closedSet.contains(currentId)) {
                continue;
            }
            closedSet.add(currentId);

            List<GraphEdge> neighbors = adjacencyList.getOrDefault(currentId, Collections.emptyList());
            for (GraphEdge edge : neighbors) {
                Long neighborId = edge.getTargetNodeId();
                if (closedSet.contains(neighborId)) {
                    continue;
                }

                double tentativeGCost = current.gCost + edge.getEffectiveCost();
                double currentBestGCost = gScores.getOrDefault(neighborId, Double.MAX_VALUE);

                if (tentativeGCost < currentBestGCost) {
                    gScores.put(neighborId, tentativeGCost);
                    cameFrom.put(neighborId, currentId);
                    cameFromEdge.put(neighborId, edge);

                    RouteNode neighborNode = nodeMap.get(neighborId);
                    double hCost = (neighborNode != null) ? calculateHeuristic(neighborNode, goal) : 0.0;
                    openSet.add(new NodeRecord(neighborId, tentativeGCost, hCost));
                }
            }
        }

        log.warn("A* Path not found between start node {} and goal node {}", startId, goalId);
        return AStarResult.builder()
                .path(Collections.emptyList())
                .totalDistanceMeters(0.0)
                .totalCost(0.0)
                .found(false)
                .build();
    }

    /**
     * 휴리스틱 함수 h(n): 현재 노드에서 목적지 노드까지의 직선 거리 (Haversine 공식)
     * 최소 가중 배수가 1.0(원활) 이상이므로 직선거리는 항상 실제 이동비용 이하(Admissible)를 보장합니다.
     */
    private static double calculateHeuristic(RouteNode current, RouteNode goal) {
        if (current.getLatitude() == null || current.getLongitude() == null
                || goal.getLatitude() == null || goal.getLongitude() == null) {
            return 0.0;
        }
        return GeoUtil.calculateDistance(current.getLatitude(), current.getLongitude(),
                goal.getLatitude(), goal.getLongitude());
    }

    private static AStarResult reconstructPath(Long startId,
                                              Long goalId,
                                              Map<Long, Long> cameFrom,
                                              Map<Long, GraphEdge> cameFromEdge,
                                              Map<Long, RouteNode> nodeMap) {
        List<RouteNode> path = new ArrayList<>();
        double totalDistance = 0.0;
        double totalCost = 0.0;

        Long curr = goalId;
        while (curr != null) {
            RouteNode node = nodeMap.get(curr);
            if (node != null) {
                path.add(node);
            }
            GraphEdge edge = cameFromEdge.get(curr);
            if (edge != null) {
                totalDistance += edge.getBaseDistanceMeters();
                totalCost += edge.getEffectiveCost();
            }
            if (curr.equals(startId)) {
                break;
            }
            curr = cameFrom.get(curr);
        }

        Collections.reverse(path);

        return AStarResult.builder()
                .path(path)
                .totalDistanceMeters(totalDistance)
                .totalCost(totalCost)
                .found(true)
                .build();
    }
}
