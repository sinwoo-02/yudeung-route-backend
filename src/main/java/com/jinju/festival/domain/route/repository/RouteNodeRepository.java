package com.jinju.festival.domain.route.repository;

import com.jinju.festival.domain.route.entity.RouteNode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface RouteNodeRepository extends JpaRepository<RouteNode, Long> {
    List<RouteNode> findByTouristZone_ZoneId(Long zoneId);

    @Query("SELECT n FROM RouteNode n LEFT JOIN FETCH n.touristZone")
    List<RouteNode> findAllWithTouristZone();
}
