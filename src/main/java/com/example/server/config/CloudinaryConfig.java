package com.example.server.config;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CloudinaryConfig {

    @Bean
    public Cloudinary cloudinary() {
        return new Cloudinary(ObjectUtils.asMap(
                "cloud_name", "dop4mtq1t",
                "api_key", "293689374536852",
                "api_secret", "dhAhHNVpXSpMicpMIFLd8a4F3FM",
                "secure", true
        ));
    }
}
