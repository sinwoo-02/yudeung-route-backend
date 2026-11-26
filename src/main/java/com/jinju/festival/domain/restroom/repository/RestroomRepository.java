package com.jinju.festival.domain.restroom.repository;

import com.jinju.festival.domain.restroom.entity.Restroom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RestroomRepository extends JpaRepository<Restroom, Long> {
    List<Restroom> findByFestival_FestivalId(Long festivalId);
}

