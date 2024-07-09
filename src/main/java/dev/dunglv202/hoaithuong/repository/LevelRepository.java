package dev.dunglv202.hoaithuong.repository;

import dev.dunglv202.hoaithuong.entity.Level;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LevelRepository extends JpaRepository<Level, Integer> {
    Optional<Level> findByCode(String code);
}
