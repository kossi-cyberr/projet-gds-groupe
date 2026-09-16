package com.example.versuion.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfiguration {

    private static final String GET = "GET";
    private static final String POST = "POST";
    private static final String DELETE = "DELETE";
    private static final String PUT = "PUT";
    private static final String PATCH = "PATCH";

    /**
     * Origines autorisees pour le CORS. Par defaut : localhost (dev).
     * En production, definir APP_CORS_ALLOWED_ORIGINS, ex.:
     * APP_CORS_ALLOWED_ORIGINS=https://stockflow.exemple.com,https://www.exemple.com
     */
    @Value("${app.cors.allowed-origins:http://localhost:3000}")
    private String allowedOrigins;



    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedMethods(GET, POST, PUT, DELETE, PATCH)
                        .allowedHeaders("*")
                        .allowedOriginPatterns(allowedOrigins.split(","))
                        .allowCredentials(true);
            }
        };
    }

}
