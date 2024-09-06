package dev.dunglv202.hoaithuong.service.impl;

import dev.dunglv202.hoaithuong.entity.Configuration;
import dev.dunglv202.hoaithuong.entity.User;
import dev.dunglv202.hoaithuong.repository.ConfigurationRepository;
import dev.dunglv202.hoaithuong.service.ConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ConfigServiceImpl implements ConfigService {
    private final ConfigurationRepository configurationRepository;

    @Override
    @Cacheable(cacheNames = "configs", key = "#user.id")
    public Configuration getConfigsByUser(User user) {
        return configurationRepository.findByUser(user).orElse(new Configuration(user));
    }

    @Override
    @CacheEvict(cacheNames = "configs", key = "#configs.user.id")
    public void saveConfigs(Configuration configs) {
        configurationRepository.save(configs);
    }
}
