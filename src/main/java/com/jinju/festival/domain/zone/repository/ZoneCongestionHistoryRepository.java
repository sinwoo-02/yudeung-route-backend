package com.jinju.festival.domain.zone.repository;

import com.jinju.festival.domain.zone.entity.ZoneCongestionHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ZoneCongestionHistoryRepository extends JpaRepository<ZoneCongestionHistory, Long> {

    List<ZoneCongestionHistory> findByTouristZone_ZoneIdOrderByCreatedAtDesc(Long zoneId);

    Optional<ZoneCongestionHistory> findFirstByTouristZone_ZoneIdOrderByCreatedAtDesc(Long zoneId);

    @Query("SELECT h FROM ZoneCongestionHistory h WHERE h.historyId IN " +
           "(SELECT MAX(h2.historyId) FROM ZoneCongestionHistory h2 GROUP BY h2.touristZone.zoneId)")
    List<ZoneCongestionHistory> findLatestCongestionForAllZones();

    @Query("SELECT h FROM ZoneCongestionHistory h WHERE h.touristZone.zoneId = :zoneId ORDER BY h.createdAt DESC LIMIT 1")
    Optional<ZoneCongestionHistory> findLatestByZoneId(@Param("zoneId") Long zoneId);
}
