package com.jinju.festival.domain.parking.repository;

import com.jinju.festival.domain.parking.entity.Parking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ParkingRepository extends JpaRepository<Parking, Long> {
    List<Parking> findByFestival_FestivalId(Long festivalId);
    Optional<Parking> findByName(String name);
    Optional<Parking> findByExternalCode(String externalCode);
}
