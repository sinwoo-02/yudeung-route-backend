package com.jinju.festival.domain.restroom.entity;

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
@Table(name = "restrooms")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Restroom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "restroom_id")
    private Long restroomId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "festival_id")
    private Festival festival;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "restroom_type")
    private String restroomType;

    @Column(name = "latitude", precision = 18, scale = 15, nullable = false)
    private BigDecimal latitude;

    @Column(name = "longitude", precision = 18, scale = 15, nullable = false)
    private BigDecimal longitude;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public Restroom(Long restroomId, Festival festival, String name,
                    String restroomType, BigDecimal latitude, BigDecimal longitude) {
        this.restroomId = restroomId;
        this.festival = festival;
        this.name = name;
        this.restroomType = restroomType;
        this.latitude = latitude;
        this.longitude = longitude;
    }
}
