package com.jinju.festival.domain.zone.entity;

import com.jinju.festival.domain.festival.entity.Festival;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
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
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "tourist_zones")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class TouristZone {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "zone_id")
    private Long zoneId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "festival_id")
    private Festival festival;

    @Column(name = "zone_name", nullable = false)
    private String zoneName;

    @Column(name = "latitude", precision = 11, scale = 8)
    private BigDecimal latitude;

    @Column(name = "longitude", precision = 11, scale = 8)
    private BigDecimal longitude;

    @Column(name = "coverage_radius_meters", nullable = false, columnDefinition = "INT DEFAULT 40")
    private Integer coverageRadiusMeters = 40;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public TouristZone(Long zoneId, Festival festival, String zoneName, BigDecimal latitude, BigDecimal longitude, Integer coverageRadiusMeters) {
        this.zoneId = zoneId;
        this.festival = festival;
        this.zoneName = zoneName;
        this.latitude = latitude;
        this.longitude = longitude;
        this.coverageRadiusMeters = (coverageRadiusMeters != null) ? coverageRadiusMeters : 40;
    }
}
