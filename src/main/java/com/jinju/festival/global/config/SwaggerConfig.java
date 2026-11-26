package com.jinju.festival.global.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

        @Bean
        public OpenAPI jinjuFestivalOpenAPI() {
                return new OpenAPI()
                                .info(new Info()
                                                .title("진주시 유등축제 실시간 주차장 안내 및 최적 경로 추천 API")
                                                .description("공공데이터포털 실시간 주차장 정보와 CCTV 구역별 혼잡도를 반영한 A* 최적 이동 경로 추천 백엔드 REST API 명세서입니다.")
                                                .version("v1.0.0")
                                                .contact(new Contact()
                                                                .name("Jinju Festival Capstone Team")
                                                                .url("https://github.com/sinwoo-02/yudeung-route-backend")))
                                .servers(List.of(
                                                new Server().url("/").description("현재 호스트 서버"),
                                                new Server().url("http://localhost:8080").description("로컬 개발 서버")));
        }
}
