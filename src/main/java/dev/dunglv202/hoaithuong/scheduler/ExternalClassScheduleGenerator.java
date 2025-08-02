package dev.dunglv202.hoaithuong.scheduler;

import dev.dunglv202.hoaithuong.entity.Schedule;
import dev.dunglv202.hoaithuong.entity.TutorClass;
import dev.dunglv202.hoaithuong.repository.ScheduleRepository;
import dev.dunglv202.hoaithuong.repository.TutorClassRepository;
import dev.dunglv202.hoaithuong.repository.UserRepository;
import dev.dunglv202.hoaithuong.service.ScheduleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class ExternalClassScheduleGenerator {
    private final UserRepository userRepository;
    private final TutorClassRepository tutorClassRepository;
    private final ScheduleService scheduleService;
    private final ScheduleRepository scheduleRepository;

    @Scheduled(cron = "${cron.schedule.generate-external-schedules}")
    public void generateExternalClassSchedules() {
        LocalDate today = LocalDate.now();
        LocalDate until = YearMonth.from(today).plusMonths(2).atEndOfMonth();

        userRepository.findAll().forEach(user -> {
            List<TutorClass> externalClasses = tutorClassRepository.findAllExternalByTeacher(user);
            log.info("Generating external classes schedule for teacher #{}", user.getId());

            externalClasses.forEach(cls -> {
                Schedule lastSchedule = scheduleRepository.findLastByTutorClass(cls);
                LocalDate from = lastSchedule.getStartTime().toLocalDate().plusDays(1);

                log.info("Generating schedule for external class #{} from {} to {}", cls.getId(), from, until);
                try {
                    scheduleService.addSchedulesExternalClass(cls, from, until);
                    log.info("Done generating schedule for external class #{} from {} to {}", cls.getId(), from, until);
                } catch (Exception e) {
                    log.error(
                        "Error while generating schedule for external class #{} from {} to {}",
                        cls.getId(), from, until, e
                    );
                }
            });

            log.info("Done generating external classes schedule for teacher #{}", user.getId());
        });
    }
}
