package dev.dunglv202.hoaithuong.config.prop;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "microsoft")
@Getter
@Setter
public class MicrosoftProperties {
    private String tenantId;
    private String clientId;
    private String clientSecret;
    private String driveId;
}
