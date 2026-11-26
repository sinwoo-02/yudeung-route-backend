package com.jinju.festival.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Value("${api.data-portal.base-url}")
    private String dataPortalBaseUrl;

    @Value("${api.kakao.base-url}")
    private String kakaoBaseUrl;

    @Value("${api.kakao.navi-base-url}")
    private String kakaoNaviBaseUrl;

    @Value("${api.kakao.rest-api-key}")
    private String kakaoRestApiKey;

    @Bean
    public RestClient dataPortalRestClient() {
        return RestClient.builder()
                .baseUrl(dataPortalBaseUrl)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    @Bean
    public RestClient kakaoLocalRestClient() {
        return RestClient.builder()
                .baseUrl(kakaoBaseUrl)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "KakaoAK " + kakaoRestApiKey)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    @Bean
    public RestClient kakaoNaviRestClient() {
        return RestClient.builder()
                .baseUrl(kakaoNaviBaseUrl)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "KakaoAK " + kakaoRestApiKey)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }
}
