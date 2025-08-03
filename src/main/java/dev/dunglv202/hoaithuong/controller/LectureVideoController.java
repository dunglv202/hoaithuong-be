package dev.dunglv202.hoaithuong.controller;

import dev.dunglv202.hoaithuong.dto.GetLectureVideoReq;
import dev.dunglv202.hoaithuong.dto.LectureVideoDTO;
import dev.dunglv202.hoaithuong.service.LectureService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/video")
@RequiredArgsConstructor
public class LectureVideoController {
    private final LectureService lectureService;

    @GetMapping
    public String videoLecturePage(Model model, @Valid GetLectureVideoReq getLectureVideoReq) {
        LectureVideoDTO lectureVideo = lectureService.getLectureVideo(getLectureVideoReq);

        if (!lectureVideo.getIsIframe()) {
            return "redirect:" + lectureVideo.getUrl();
        }

        model.addAttribute("title", lectureVideo.getMetadata().getTitle());
        model.addAttribute("description", lectureVideo.getMetadata().getDescription());
        model.addAttribute("thumbnail", lectureVideo.getMetadata().getThumbnailUrl());
        model.addAttribute("iframeSrc", lectureVideo.getUrl());

        return "lecture/video";
    }
}
