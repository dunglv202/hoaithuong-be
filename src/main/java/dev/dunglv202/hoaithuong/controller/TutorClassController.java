package dev.dunglv202.hoaithuong.controller;

import dev.dunglv202.hoaithuong.dto.*;
import dev.dunglv202.hoaithuong.model.Page;
import dev.dunglv202.hoaithuong.model.Pagination;
import dev.dunglv202.hoaithuong.model.criteria.TutorClassCriteria;
import dev.dunglv202.hoaithuong.service.TutorClassService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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
    public Page<TutorClassDTO> getAllClasses(TutorClassCriteria criteria, Pagination pagination) {
        return tutorClassService.getAllClasses(criteria, pagination.limit(20));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public DetailClassDTO getDetailClass(@PathVariable long id) {
        return tutorClassService.getDetailClass(id);
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public void updateClass(@Valid @RequestBody UpdatedTutorClassDTO updatedTutorClassDTO, @PathVariable long id) {
        updatedTutorClassDTO.setId(id);
        tutorClassService.updateClass(updatedTutorClassDTO);
    }

    @PostMapping("/{id}/stop")
    @PreAuthorize("isAuthenticated()")
    public void stopClass(@PathVariable long id, @RequestBody FutureUpdateDTO updateDTO) {
        tutorClassService.stopClass(id, updateDTO.getEffectiveDate());
    }

    @PostMapping("/{id}/resume")
    @PreAuthorize("isAuthenticated()")
    public void resumeClass(@PathVariable long id, @RequestBody FutureUpdateDTO updateDTO) {
        tutorClassService.resumeClass(id, updateDTO.getEffectiveDate());
    }
}
