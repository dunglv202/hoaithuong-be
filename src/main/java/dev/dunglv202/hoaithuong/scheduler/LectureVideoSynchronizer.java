package dev.dunglv202.hoaithuong.scheduler;

import dev.dunglv202.hoaithuong.entity.Lecture;
import dev.dunglv202.hoaithuong.entity.User;
import dev.dunglv202.hoaithuong.model.Range;
import dev.dunglv202.hoaithuong.model.ReportRange;
import dev.dunglv202.hoaithuong.repository.LectureRepository;
import dev.dunglv202.hoaithuong.repository.UserRepository;
import dev.dunglv202.hoaithuong.service.VideoStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class LectureVideoSynchronizer {
    private final UserRepository userRepository;
    private final LectureRepository lectureRepository;
    private final VideoStorageService videoStorageService;

    @Scheduled(cron = "${cron.schedule.sync-video}")
    public void syncLectureVideos() {
        LocalDate now = LocalDate.now();
        ReportRange range = new ReportRange();
        range.setMonth(now.getMonthValue());
        range.setYear(now.getYear());

        userRepository.findAll().forEach(user -> {
            log.info("Start lecture video synchronization for {} month: {}, year: {}", user, range.getMonth(), range.getYear());
            syncLectureVideos(user, range);
            log.info("Finished video synchronization report for {}", user);
        });
    }

    private void syncLectureVideos(User teacher, Range<LocalDate> range) {
        List<Lecture> lectures = lectureRepository.findAllInRangeByTeacher(teacher, range);
        lectures.forEach(lecture -> {
            try {
                Optional<String> videoUrl = videoStorageService.getLectureVideo(lecture);
                if (videoUrl.isPresent()) {
                    lecture.setVideo(videoUrl.get());
                    lectureRepository.save(lecture);
                } else {
                    log.info("No video found for lecture {}", lecture);
                }
            } catch (Exception e) {
                log.error("Error occurred while syncing lecture video - lecture: {}", lecture, e);
            }
        });
    }
}
