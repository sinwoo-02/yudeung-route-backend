package com.jinju.festival.domain.route.repository;

import com.jinju.festival.domain.route.entity.RouteEdge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface RouteEdgeRepository extends JpaRepository<RouteEdge, Long> {
    List<RouteEdge> findByStartNode_NodeId(Long startNodeId);
    List<RouteEdge> findByStartNode_NodeIdOrEndNode_NodeId(Long startNodeId, Long endNodeId);

    @Query("SELECT e FROM RouteEdge e JOIN FETCH e.startNode JOIN FETCH e.endNode")
    List<RouteEdge> findAllWithNodes();
}
