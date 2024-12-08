package dev.dunglv202.hoaithuong.config;

import dev.dunglv202.hoaithuong.config.prop.MinIOProperties;
import io.minio.MinioClient;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class MinIOConfig {
    private final MinIOProperties minIOProperties;

    @Bean
    public MinioClient minioClient() {
        return MinioClient.builder()
            .endpoint(minIOProperties.getUrl())
            .credentials(minIOProperties.getAccessKey(), minIOProperties.getAccessSecret())
            .build();
    }
}
