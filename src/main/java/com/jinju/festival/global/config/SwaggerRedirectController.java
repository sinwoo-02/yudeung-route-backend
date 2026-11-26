package com.jinju.festival.global.config;

import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Hidden
@Controller
@ConditionalOnProperty(name = "springdoc.swagger-ui.enabled", havingValue = "true", matchIfMissing = true)
public class SwaggerRedirectController {

    @GetMapping({"/", "/swagger", "/swagger/", "/swagger-ui", "/swagger-ui/", "/docs", "/docs/", "/api-docs", "/api-docs/"})
    public String redirectToSwaggerUi() {
        return "redirect:/swagger-ui/index.html";
    }
}
