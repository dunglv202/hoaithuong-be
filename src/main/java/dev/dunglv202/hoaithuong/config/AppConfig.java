package dev.dunglv202.hoaithuong.config;

import dev.dunglv202.hoaithuong.entity.User;
import dev.dunglv202.hoaithuong.model.SecurityAuditorAware;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.Locale;

@Configuration
@EnableAspectJAutoProxy
@EnableAsync
@EnableJpaAuditing
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
}
