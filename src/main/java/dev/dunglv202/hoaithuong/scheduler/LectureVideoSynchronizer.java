package dev.dunglv202.hoaithuong.scheduler;

import dev.dunglv202.hoaithuong.model.ReportRange;
import dev.dunglv202.hoaithuong.repository.UserRepository;
import dev.dunglv202.hoaithuong.service.LectureService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
@Slf4j
public class LectureVideoSynchronizer {
    private final UserRepository userRepository;
    private final LectureService lectureService;

    @Scheduled(cron = "${cron.schedule.sync-video}")
    public void syncLectureVideos() {
        LocalDate now = LocalDate.now();
        ReportRange range = new ReportRange();
        range.setMonth(now.getMonthValue());
        range.setYear(now.getYear());

        userRepository.findAll().forEach(user -> {
            log.info("Start lecture video synchronization for {} month: {}, year: {}", user, range.getMonth(), range.getYear());
            lectureService.syncLectureVideos(user, range);
            log.info("Finished video synchronization report for {}", user);
        });
    }
}
