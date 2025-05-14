package dev.dunglv202.hoaithuong.scheduler;

import dev.dunglv202.hoaithuong.entity.Lecture;
import dev.dunglv202.hoaithuong.repository.LectureRepository;
import dev.dunglv202.hoaithuong.service.VideoStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

/**
 * Video link should be revoked after a while to avoid error
 * "too many anonymous link request..." from Microsoft
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class VideoLinkRevoker {
    private final LectureRepository lectureRepository;
    private final VideoStorageService videoStorageService;

    @Scheduled(cron = "${cron.schedule.revoke-video-url}")
    public void revokeExpiredVideos() {
        List<Lecture> needRevoking = lectureRepository.findAllForRevokingVideoUrl(LocalDate.now());
        needRevoking.forEach(lecture -> {
            try {
                videoStorageService.revokeSharableLink(lecture.getVideoId());
            } catch (Exception e) {
                log.error(
                    "Could not revoke shared url for lecture #{}, item #{}",
                    lecture.getId(), lecture.getVideoId(), e
                );
            }
        });
    }
}
