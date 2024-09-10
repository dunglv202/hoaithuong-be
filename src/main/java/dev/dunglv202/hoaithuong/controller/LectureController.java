package dev.dunglv202.hoaithuong.controller;

import dev.dunglv202.hoaithuong.dto.LectureDTO;
import dev.dunglv202.hoaithuong.dto.NewLectureDTO;
import dev.dunglv202.hoaithuong.dto.UpdatedLecture;
import dev.dunglv202.hoaithuong.model.ReportRange;
import dev.dunglv202.hoaithuong.service.LectureService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/lectures")
@RequiredArgsConstructor
public class LectureController {
    private final LectureService lectureService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public void addNewLecture(@Valid @RequestBody NewLectureDTO newLectureDTO) {
        lectureService.addNewLecture(newLectureDTO);
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public List<LectureDTO> getAllLectures(@Valid ReportRange range) {
        return lectureService.getAllLectures(range);
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public void updateLecture(@Valid @RequestBody UpdatedLecture updatedLecture, @PathVariable long id) {
        updatedLecture.setId(id);
        lectureService.updateLecture(updatedLecture);
    }
}
