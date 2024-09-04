package dev.dunglv202.hoaithuong.scheduler;

import dev.dunglv202.hoaithuong.model.ReportRange;
import dev.dunglv202.hoaithuong.repository.UserRepository;
import dev.dunglv202.hoaithuong.service.ReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReportExporter {
    private final ReportService reportService;
    private final UserRepository userRepository;

    @Async
    @Scheduled(cron = "${cron.schedule.export-report}")
    public void exportReportToGoogleSheet() {
        LocalDate now = LocalDate.now();
        ReportRange range = new ReportRange();
        range.setMonth(now.getMonthValue());
        range.setYear(now.getYear());
        userRepository.findAll().forEach(user -> {
            try {
                log.info("Start exporting report for {} month: {}, year: {}", user.getUsername(), range.getMonth(), range.getYear());
                reportService.exportGoogleSheet(user, range);
                log.info("Finished exporting report for {}", user.getUsername());
            } catch (Exception e) {
                log.error("Could not export report for {} to google sheet", user.getUsername(), e);
            }
        });
    }
}