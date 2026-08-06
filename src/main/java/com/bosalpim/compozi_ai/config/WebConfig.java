package com.bosalpim.compozi_ai.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // OpenAPI 명세 경로 CORS 허용
                .allowedOrigins(
                        "http://mvp-alb-1710523618.ap-northeast-2.elb.amazonaws.com/swagger-ui/index.html") // 배포된 Swagger UI URL
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*");
    }
}
