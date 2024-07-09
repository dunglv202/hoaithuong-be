package dev.dunglv202.hoaithuong.service;

import dev.dunglv202.hoaithuong.dto.LevelDTO;
import dev.dunglv202.hoaithuong.entity.Level;
import dev.dunglv202.hoaithuong.repository.LevelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LevelService {
    private final LevelRepository levelRepository;

    @Cacheable("levelDTOs")
    public List<LevelDTO> getAllLevels() {
        return levelRepository.findAll().stream()
            .sorted(Comparator.comparing(Level::getSortOrder))
            .map(LevelDTO::new)
            .toList();
    }
}
