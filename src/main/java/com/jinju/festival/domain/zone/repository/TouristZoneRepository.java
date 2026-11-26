package com.jinju.festival.domain.zone.repository;

import com.jinju.festival.domain.zone.entity.TouristZone;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TouristZoneRepository extends JpaRepository<TouristZone, Long> {
    List<TouristZone> findByFestival_FestivalId(Long festivalId);
}
