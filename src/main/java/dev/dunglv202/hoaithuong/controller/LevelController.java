package dev.dunglv202.hoaithuong.controller;

import dev.dunglv202.hoaithuong.dto.LevelDTO;
import dev.dunglv202.hoaithuong.service.LevelService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/levels")
@RequiredArgsConstructor
public class LevelController {
    private final LevelService levelService;

    @GetMapping
    public List<LevelDTO> getAllLevels() {
        return levelService.getAllLevels();
    }
}
