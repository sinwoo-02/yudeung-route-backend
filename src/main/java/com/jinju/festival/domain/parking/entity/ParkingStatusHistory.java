package com.jinju.festival.domain.parking.entity;

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
@Table(name = "parkings_status_history")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class ParkingStatusHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "history_id")
    private Long historyId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parking_id")
    private Parking parking;

    @Column(name = "current_parking_count")
    private Integer currentParkingCount;

    @Column(name = "used_space")
    private Integer usedSpace;

    @Column(name = "used_dis_space")
    private Integer usedDisSpace;

    @Column(name = "available_spaces")
    private Integer availableSpaces;

    @Column(name = "occupancy_rate", precision = 5, scale = 2)
    private BigDecimal occupancyRate;

    @Column(name = "is_full")
    private Boolean isFull;

    @Column(name = "congestion_status")
    private String congestionStatus;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public ParkingStatusHistory(Long historyId, Parking parking, Integer currentParkingCount, Integer usedSpace,
                                Integer usedDisSpace, Integer availableSpaces, BigDecimal occupancyRate,
                                Boolean isFull, String congestionStatus) {
        this.historyId = historyId;
        this.parking = parking;
        this.currentParkingCount = currentParkingCount;
        this.usedSpace = usedSpace;
        this.usedDisSpace = usedDisSpace;
        this.availableSpaces = availableSpaces;
        this.occupancyRate = occupancyRate;
        this.isFull = isFull;
        this.congestionStatus = congestionStatus;
    }
}
