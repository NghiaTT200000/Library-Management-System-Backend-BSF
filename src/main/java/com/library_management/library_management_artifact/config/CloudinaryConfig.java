package com.library_management.library_management_artifact.config;

import java.util.HashMap;
import java.util.Map;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.cloudinary.Cloudinary;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class CloudinaryConfig {

    private final AppProperties appProperties;

    @Bean
    public Cloudinary cloudinary() {
        AppProperties.Cloudinary cfg = appProperties.getCloudinary();
        Map<String, String> config = new HashMap<>();
        config.put("cloud_name", cfg.getCloudName());
        config.put("api_key", cfg.getApiKey());
        config.put("api_secret", cfg.getApiSecret());
        config.put("secure", "true");
        return new Cloudinary(config);
    }
}
