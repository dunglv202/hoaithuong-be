package dev.dunglv202.hoaithuong.config;

import dev.dunglv202.hoaithuong.entity.User;
import dev.dunglv202.hoaithuong.helper.IdEncryptor;
import dev.dunglv202.hoaithuong.model.SecurityAuditorAware;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Primary;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import java.util.Locale;

@Configuration
@EnableAspectJAutoProxy
@EnableAsync
@EnableJpaAuditing
@EnableScheduling
@EnableCaching
public class AppConfig {
    @Bean
    public MessageSource messageSource() {
        final ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
        messageSource.setBasename("classpath:messages");
        messageSource.setDefaultEncoding("UTF-8");
        messageSource.setDefaultLocale(Locale.US);
        return messageSource;
    }

    @Bean
    public AuditorAware<User> auditorProvider() {
        return new SecurityAuditorAware();
    }

    @Bean
    @Primary
    public ThreadPoolTaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setCorePoolSize(5);
        taskExecutor.setMaxPoolSize(10);
        taskExecutor.setQueueCapacity(25);
        return taskExecutor;
    }

    @Bean
    public IdEncryptor idEncryptor(@Value("${security.id-encryptor.secret}") String idEncryptorSecret) {
        byte[] keyBytes = Base64.getDecoder().decode(idEncryptorSecret);
        SecretKey secretKey = new SecretKeySpec(keyBytes, "AES");
        return new IdEncryptor(secretKey);
    }
}
