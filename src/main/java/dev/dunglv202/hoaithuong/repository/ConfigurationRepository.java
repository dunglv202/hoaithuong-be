package dev.dunglv202.hoaithuong.repository;

import dev.dunglv202.hoaithuong.entity.Configuration;
import dev.dunglv202.hoaithuong.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ConfigurationRepository extends JpaRepository<Configuration, Long> {
    Optional<Configuration> findByUser(User user);
}
