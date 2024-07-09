package dev.dunglv202.hoaithuong.controller;

import dev.dunglv202.hoaithuong.dto.NewLectureDTO;
import dev.dunglv202.hoaithuong.service.LectureService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/lectures")
@RequiredArgsConstructor
public class LectureController {
    private final LectureService lectureService;

    @PostMapping
//    @PreAuthorize("isAuthenticated()")
    public void addNewLecture(@Valid @RequestBody NewLectureDTO newLectureDTO) {
        lectureService.addNewLecture(newLectureDTO);
    }
}
