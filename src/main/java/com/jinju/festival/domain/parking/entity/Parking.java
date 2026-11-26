package com.jinju.festival.domain.parking.entity;

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
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "parkings")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Parking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "parking_id")
    private Long parkingId;

    @Column(name = "external_code", length = 50, unique = true)
    private String externalCode; // 공공데이터포털 주차장 코드 (idx 등)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "festival_id")
    private Festival festival;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "address")
    private String address;

    @Column(name = "parking_type")
    private String parkingType;

    @Column(name = "latitude", precision = 10, scale = 7)
    private BigDecimal latitude;

    @Column(name = "longitude", precision = 10, scale = 7)
    private BigDecimal longitude;

    @Column(name = "total_space")
    private Integer totalSpace;

    @Column(name = "dis_space")
    private Integer disSpace;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public Parking(Long parkingId, String externalCode, Festival festival, String name, String address, String parkingType,
                   BigDecimal latitude, BigDecimal longitude, Integer totalSpace, Integer disSpace) {
        this.parkingId = parkingId;
        this.externalCode = externalCode;
        this.festival = festival;
        this.name = name;
        this.address = address;
        this.parkingType = parkingType;
        this.latitude = latitude;
        this.longitude = longitude;
        this.totalSpace = totalSpace;
        this.disSpace = disSpace;
    }

    public void updateCoordinates(BigDecimal latitude, BigDecimal longitude) {
        if (latitude != null && longitude != null) {
            this.latitude = latitude;
            this.longitude = longitude;
        }
    }

    public void updateAddress(String address) {
        if (address != null && !address.isBlank()) {
            this.address = address;
        }
    }

    public void updateExternalCode(String externalCode) {
        if (externalCode != null && !externalCode.isBlank()) {
            this.externalCode = externalCode;
        }
    }

    public void updateDetails(String address, BigDecimal latitude, BigDecimal longitude, Integer totalSpace) {
        if (address != null && !address.isBlank()) {
            this.address = address;
        }
        if (latitude != null && longitude != null) {
            this.latitude = latitude;
            this.longitude = longitude;
        }
        if (totalSpace != null && totalSpace > 0) {
            this.totalSpace = totalSpace;
        }
    }
}
