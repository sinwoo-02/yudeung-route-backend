package com.jinju.festival.domain.festival.service;

import com.jinju.festival.domain.festival.dto.FestivalDto;
import com.jinju.festival.domain.festival.entity.Festival;
import com.jinju.festival.domain.festival.repository.FestivalRepository;
import com.jinju.festival.global.error.exception.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FestivalService {

    private final FestivalRepository festivalRepository;

    public List<FestivalDto> getAllFestivals() {
        return festivalRepository.findAll().stream()
                .map(FestivalDto::from)
                .toList();
    }

    public FestivalDto getFestival(Long festivalId) {
        Festival festival = festivalRepository.findById(festivalId)
                .orElseThrow(() -> new EntityNotFoundException("해당 축제를 찾을 수 없습니다: " + festivalId));
        return FestivalDto.from(festival);
    }
}
