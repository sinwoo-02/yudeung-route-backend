package com.jinju.festival.domain.parking.repository;

import com.jinju.festival.domain.parking.entity.ParkingStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ParkingStatusHistoryRepository extends JpaRepository<ParkingStatusHistory, Long> {

    List<ParkingStatusHistory> findByParking_ParkingIdOrderByCreatedAtDesc(Long parkingId);

    Optional<ParkingStatusHistory> findFirstByParking_ParkingIdOrderByCreatedAtDesc(Long parkingId);

    @Query("SELECT h FROM ParkingStatusHistory h WHERE h.historyId IN " +
           "(SELECT MAX(h2.historyId) FROM ParkingStatusHistory h2 GROUP BY h2.parking.parkingId)")
    List<ParkingStatusHistory> findLatestStatusForAllParkings();

    @Query("SELECT h FROM ParkingStatusHistory h WHERE h.parking.parkingId = :parkingId ORDER BY h.createdAt DESC LIMIT 1")
    Optional<ParkingStatusHistory> findLatestByParkingId(@Param("parkingId") Long parkingId);
}
