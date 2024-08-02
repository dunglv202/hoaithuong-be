package dev.dunglv202.hoaithuong.service.impl;

import dev.dunglv202.hoaithuong.dto.ScheduleDTO;
import dev.dunglv202.hoaithuong.entity.Schedule;
import dev.dunglv202.hoaithuong.entity.TutorClass;
import dev.dunglv202.hoaithuong.exception.ClientVisibleException;
import dev.dunglv202.hoaithuong.exception.ConflictScheduleException;
import dev.dunglv202.hoaithuong.model.Range;
import dev.dunglv202.hoaithuong.model.TimeSlot;
import dev.dunglv202.hoaithuong.repository.ScheduleRepository;
import dev.dunglv202.hoaithuong.service.ScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ScheduleServiceImpl implements ScheduleService {
    private final ScheduleRepository scheduleRepository;

    @Override
    public List<ScheduleDTO> getSchedule(Range<LocalDate> range) {
        return scheduleRepository.findAllInRange(range.getFrom(), range.getTo())
            .stream()
            .map(ScheduleDTO::new)
            .toList();
    }

    @Override
    public void deleteSchedule(Long id) {
        Schedule schedule = scheduleRepository.findById(id)
            .orElseThrow();
        if (schedule.getLecture() != null) throw new ClientVisibleException("{schedule.attached_to_lecture}");
        scheduleRepository.delete(schedule);
    }

    /**
     * Add new class to schedule if that class have timeslots
     */
    @Override
    public void addClassToMySchedule(TutorClass newClass, LocalDate startDate) {
        List<Schedule> schedules = makeScheduleForClass(newClass, startDate);

        if (schedules.isEmpty()) return;

        // check for conflicts with other schedules
        Schedule firstSchedule = schedules.get(0);
        Schedule lastSchedule = schedules.get(schedules.size() - 1);
        List<Schedule> activeSchedules = scheduleRepository.findAllInRange(
            firstSchedule.getStartTime().toLocalDate(),
            lastSchedule.getEndTime().toLocalDate()
        );
        for (Schedule schedule : schedules) {
            for (Schedule scheduled : activeSchedules) {
                if (scheduled.isAfter(schedule)) break;
                if (schedule.overlaps(scheduled)) {
                    throw new ConflictScheduleException();
                }
            }
        }

        // no conflicts => save schedule
        scheduleRepository.saveAll(schedules);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Schedule addSingleScheduleForClass(TutorClass tutorClass, LocalDateTime startTime) {
        Schedule schedule = makeSchedule(tutorClass, startTime);

        // check if conflict TODO: same sub-logic with #addClassToMySchedule() => might separate to another method
        List<Schedule> schedulesInSameDay = scheduleRepository.findAllInDate(schedule.getStartTime().toLocalDate());
        for (Schedule scheduled : schedulesInSameDay) {
            if (schedule.overlaps(scheduled)) {
                throw new ConflictScheduleException();
            }
        }

        // replace last schedule if needed
        int totalScheduled = scheduleRepository.countByTutorClass(tutorClass);
        int totalScheduleShouldHave = tutorClass.getTotalLecture() - tutorClass.getInitialLearned();
        if (totalScheduled == totalScheduleShouldHave) {
            Schedule lastSchedule = scheduleRepository.findLastByTutorClass(tutorClass);
            scheduleRepository.delete(lastSchedule);
        }

        scheduleRepository.save(schedule);

        return schedule;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateScheduleForClass(TutorClass tutorClass, LocalDate startDate, Set<TimeSlot> timeSlots) {
        scheduleRepository.deleteAllFromDateByClass(tutorClass, startDate);
        tutorClass.setTimeSlots(new ArrayList<>(timeSlots));
        addClassToMySchedule(tutorClass, startDate);
    }

    /**
     * Make schedule list in ascending order by date and time for tutor class
     */
    private List<Schedule> makeScheduleForClass(TutorClass tutorClass, LocalDate startDate) {
        if (tutorClass.getTimeSlots() == null || tutorClass.getTimeSlots().isEmpty()) {
            return List.of();
        }

        List<Schedule> schedules = new ArrayList<>();
        LocalDate date = startDate;
        int numOfLecture = tutorClass.getTotalLecture() - tutorClass.getLearned();

        // sort time slots by ascending order
        Collections.sort(tutorClass.getTimeSlots());

        while (numOfLecture > 0) {
            LocalDate today = date;
            List<TimeSlot> todayTimeSlots = tutorClass.getTimeSlots().stream()
                .filter(t -> t.getWeekday() == today.getDayOfWeek())
                .toList();

            // add schedule for each time slot of day if there is still lecture left
            for (TimeSlot timeSlot : todayTimeSlots) {
                if (numOfLecture <= 0) break;
                schedules.add(makeSchedule(tutorClass, date.atTime(timeSlot.getStartTime())));
                numOfLecture--;
            }

            date = date.plusDays(1);
        }

        return schedules;
    }

    private Schedule makeSchedule(TutorClass tutorClass, LocalDateTime startTime) {
        Schedule schedule = new Schedule();
        schedule.setStartTime(startTime);
        schedule.setEndTime(schedule.getStartTime().plus(tutorClass.getDuration()));
        schedule.setTutorClass(tutorClass);

        return schedule;
    }
}