package dev.dunglv202.hoaithuong.controller;

import dev.dunglv202.hoaithuong.dto.NewTutorClassDTO;
import dev.dunglv202.hoaithuong.dto.TutorClassDTO;
import dev.dunglv202.hoaithuong.model.TutorClassCriteria;
import dev.dunglv202.hoaithuong.service.impl.TutorClassService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tutor_classes")
@RequiredArgsConstructor
public class TutorClassController {
    private final TutorClassService tutorClassService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public void addNewClass(@Valid @RequestBody NewTutorClassDTO newTutorClassDTO) {
        tutorClassService.addNewClass(newTutorClassDTO);
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public List<TutorClassDTO> getAllClasses(TutorClassCriteria criteria) {
        return tutorClassService.getAllClasses(criteria);
    }
}
