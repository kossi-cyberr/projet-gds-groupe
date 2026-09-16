package com.example.versuion.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

/**
 * Sert les photos stockées localement (app.photos.dir) sous /photos/**.
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final String repertoirePhotos;

    public WebMvcConfig(@Value("${app.photos.dir:./photos}") String repertoirePhotos) {
        this.repertoirePhotos = Paths.get(repertoirePhotos).toAbsolutePath().normalize().toString();
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/photos/**")
                .addResourceLocations("file:" + repertoirePhotos + "/");
    }
}
