package com.autocare.autocarebackend.config;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class CloudinaryConfig {

    private static final Logger logger = LoggerFactory.getLogger(CloudinaryConfig.class);

    private final CloudinaryProperties cloudinaryProperties;

    public CloudinaryConfig(CloudinaryProperties cloudinaryProperties) {
        this.cloudinaryProperties = cloudinaryProperties;
    }

    @Bean
    @ConditionalOnProperty(name = "cloudinary.cloud_name")
    public Cloudinary cloudinary() {
        logger.info("🔧 Creating Cloudinary bean...");

        Map<String, Object> config = new HashMap<>();
        config.put("cloud_name", cloudinaryProperties.getCloudName());
        config.put("api_key", cloudinaryProperties.getApiKey());
        config.put("api_secret", cloudinaryProperties.getApiSecret());
        config.put("secure", true);

        Cloudinary cloudinary = new Cloudinary(config);

        // Test connection
        try {
            Map<?, ?> result = cloudinary.api().ping(ObjectUtils.emptyMap());
            logger.info("✅ Cloudinary connection successful: {}", result);
        } catch (Exception e) {
            logger.error("❌ Cloudinary connection failed: {}", e.getMessage());
            logger.error(" Please check your credentials and internet connection");
        }

        return cloudinary;
    }
}