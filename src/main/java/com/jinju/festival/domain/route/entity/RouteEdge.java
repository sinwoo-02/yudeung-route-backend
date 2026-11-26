package com.jinju.festival.domain.route.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "route_edges")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RouteEdge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "edge_id")
    private Long edgeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "start_node_id")
    private RouteNode startNode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "end_node_id")
    private RouteNode endNode;

    @Column(name = "base_distance_meter")
    private Double baseDistanceMeter;

    @Column(name = "is_bidirectional")
    private Boolean isBidirectional;

    @Builder
    public RouteEdge(Long edgeId, RouteNode startNode, RouteNode endNode, Double baseDistanceMeter, Boolean isBidirectional) {
        this.edgeId = edgeId;
        this.startNode = startNode;
        this.endNode = endNode;
        this.baseDistanceMeter = baseDistanceMeter;
        this.isBidirectional = isBidirectional;
    }
}
