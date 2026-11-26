package com.jinju.festival.domain.route.entity;

import com.jinju.festival.domain.zone.entity.TouristZone;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PostPersist;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Persistable;

import java.util.concurrent.atomic.AtomicLong;

@Entity
@Table(name = "route_nodes")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RouteNode implements Persistable<Long> {

    private static final AtomicLong FALLBACK_ID_GENERATOR = new AtomicLong(10000L);

    @Id
    @Column(name = "node_id")
    private Long nodeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "zone_id")
    private TouristZone touristZone;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    @Column(name = "node_name")
    private String nodeName;

    @Transient
    private boolean isNew = true;

    @Builder
    public RouteNode(Long nodeId, TouristZone touristZone, Double latitude, Double longitude, String nodeName) {
        this.nodeId = (nodeId != null) ? nodeId : FALLBACK_ID_GENERATOR.incrementAndGet();
        this.touristZone = touristZone;
        this.latitude = latitude;
        this.longitude = longitude;
        this.nodeName = nodeName;
    }

    @Override
    public Long getId() {
        return this.nodeId;
    }

    @Override
    public boolean isNew() {
        return this.isNew;
    }

    @PostPersist
    @PostLoad
    void markNotNew() {
        this.isNew = false;
    }
}
