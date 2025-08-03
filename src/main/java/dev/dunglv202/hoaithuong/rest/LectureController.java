package dev.dunglv202.hoaithuong.rest;

import dev.dunglv202.hoaithuong.dto.*;
import dev.dunglv202.hoaithuong.model.ReportRange;
import dev.dunglv202.hoaithuong.service.LectureService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
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

    @PostMapping(value = "/sync_videos", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("isAuthenticated()")
    public void syncLectureVideos(@Valid ReportRange range) {
        lectureService.syncMyLectureVideos(range);
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public List<LectureDTO> getAllLectures(@Valid ReportRange range) {
        return lectureService.getAllLectures(range);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public LectureDetails getLectureDetails(@PathVariable long id) {
        return lectureService.getLectureDetails(id);
    }

    @GetMapping("/{id}/preview")
    @PreAuthorize("isAuthenticated()")
    public String getVideoPreview(@PathVariable long id) {
        return lectureService.getVideoPreview(id);
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public void updateLecture(@Valid @RequestBody UpdatedLecture updatedLecture, @PathVariable long id) {
        updatedLecture.setId(id);
        lectureService.updateLecture(updatedLecture);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public void deleteLecture(@PathVariable long id) {
        lectureService.deleteLecture(id);
    }

    @GetMapping("/video")
    public LectureVideoDTO getLectureVideo(@Valid GetLectureVideoReq req) {
        return lectureService.getLectureVideo(req);
    }
}
