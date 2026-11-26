package com.jinju.festival.global.util;

import com.jinju.festival.domain.route.entity.RouteNode;

import java.util.List;

public final class GeoUtil {

    private static final double EARTH_RADIUS_METERS = 6371000.0;

    private GeoUtil() {
    }

    /**
     * 하버사인(Haversine) 공식을 이용한 두 위경도 좌표 간 직선 거리(m) 계산
     */
    public static double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_METERS * c;
    }

    /**
     * 주어진 위경도 좌표에서 가장 가까운 도로망 RouteNode 탐색
     */
    public static RouteNode findNearestNode(List<RouteNode> nodes, double targetLat, double targetLon) {
        if (nodes == null || nodes.isEmpty()) {
            throw new IllegalArgumentException("노드 목록이 비어 있어 최근접 노드를 찾을 수 없습니다.");
        }

        RouteNode nearest = null;
        double minDistance = Double.MAX_VALUE;

        for (RouteNode node : nodes) {
            if (node.getLatitude() == null || node.getLongitude() == null) {
                continue;
            }
            double distance = calculateDistance(targetLat, targetLon, node.getLatitude(), node.getLongitude());
            if (distance < minDistance) {
                minDistance = distance;
                nearest = node;
            }
        }

        if (nearest == null) {
            throw new IllegalStateException("유효한 좌표를 가진 최근접 노드가 존재하지 않습니다.");
        }

        return nearest;
    }
}
