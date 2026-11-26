package com.jinju.festival.controller;

import com.jinju.festival.domain.restroom.entity.Restroom;
import com.jinju.festival.domain.restroom.repository.RestroomRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
@SuppressWarnings("null")
class RestroomControllerTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private RestroomRepository restroomRepository;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    @DisplayName("전체 화장실 목록(이름 및 위경도 좌표) 조회 API 검증")
    void testGetAllRestrooms() throws Exception {
        mockMvc.perform(get("/api/v1/restrooms")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data", hasSize(10)))
                .andExpect(jsonPath("$.data[0].restroomId").exists())
                .andExpect(jsonPath("$.data[0].name").isString())
                .andExpect(jsonPath("$.data[0].latitude").isNumber())
                .andExpect(jsonPath("$.data[0].longitude").isNumber());
    }

    @Test
    @DisplayName("특정 화장실 상세 조회 API 검증")
    void testGetRestroomDetail() throws Exception {
        Restroom firstRestroom = restroomRepository.findAll().get(0);

        mockMvc.perform(get("/api/v1/restrooms/" + firstRestroom.getRestroomId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.restroomId").value(firstRestroom.getRestroomId()))
                .andExpect(jsonPath("$.data.name").value(firstRestroom.getName()))
                .andExpect(jsonPath("$.data.latitude").isNumber())
                .andExpect(jsonPath("$.data.longitude").isNumber());
    }
}

