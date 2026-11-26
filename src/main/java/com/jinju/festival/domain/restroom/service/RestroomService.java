package com.jinju.festival.domain.restroom.service;

import com.jinju.festival.domain.restroom.dto.RestroomDto;
import com.jinju.festival.domain.restroom.entity.Restroom;
import com.jinju.festival.domain.restroom.repository.RestroomRepository;
import com.jinju.festival.global.error.exception.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RestroomService {

    private final RestroomRepository restroomRepository;

    /**
     * 전체 화장실 위치(위경도 좌표) 및 이름 목록 조회
     */
    public List<RestroomDto> getAllRestrooms() {
        return restroomRepository.findAll().stream()
                .map(RestroomDto::from)
                .toList();
    }

    /**
     * 특정 화장실 상세 정보 조회
     */
    @SuppressWarnings("null")
    public RestroomDto getRestroom(Long restroomId) {
        Restroom restroom = restroomRepository.findById(restroomId)
                .orElseThrow(() -> new EntityNotFoundException("해당 화장실 정보를 찾을 수 없습니다: " + restroomId));
        return RestroomDto.from(restroom);
    }
}

