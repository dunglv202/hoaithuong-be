package dev.dunglv202.hoaithuong.repository;

import dev.dunglv202.hoaithuong.entity.Configuration;
import dev.dunglv202.hoaithuong.entity.User;
import dev.dunglv202.hoaithuong.service.ConfigService;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Notes: DO NOT use this directly, work with configs via {@link ConfigService}
 */
public interface ConfigurationRepository extends JpaRepository<Configuration, Long> {
    Optional<Configuration> findByUser(User user);
}
