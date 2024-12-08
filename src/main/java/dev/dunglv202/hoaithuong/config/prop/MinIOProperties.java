package dev.dunglv202.hoaithuong.config.prop;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@Getter
@Setter
@ConfigurationProperties(prefix = "minio")
public class MinIOProperties {
    private String url;
    private String accessKey;
    private String accessSecret;
    private String bucket;
}
