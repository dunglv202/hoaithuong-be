package dev.dunglv202.hoaithuong.service.impl;

import dev.dunglv202.hoaithuong.dto.MinimalScheduleDTO;
import dev.dunglv202.hoaithuong.dto.NewScheduleDTO;
import dev.dunglv202.hoaithuong.entity.*;
import dev.dunglv202.hoaithuong.exception.ClientVisibleException;
import dev.dunglv202.hoaithuong.exception.ConflictScheduleException;
import dev.dunglv202.hoaithuong.helper.AuthHelper;
import dev.dunglv202.hoaithuong.helper.ScheduleGenerator;
import dev.dunglv202.hoaithuong.mapper.ScheduleMapper;
import dev.dunglv202.hoaithuong.model.Range;
import dev.dunglv202.hoaithuong.model.ScheduleEvent;
import dev.dunglv202.hoaithuong.model.SimpleRange;
import dev.dunglv202.hoaithuong.model.TimeSlot;
import dev.dunglv202.hoaithuong.model.criteria.ScheduleCriteria;
import dev.dunglv202.hoaithuong.repository.LectureRepository;
import dev.dunglv202.hoaithuong.repository.ScheduleRepository;
import dev.dunglv202.hoaithuong.repository.TutorClassRepository;
import dev.dunglv202.hoaithuong.service.CalendarService;
import dev.dunglv202.hoaithuong.service.NotificationService;
import dev.dunglv202.hoaithuong.service.ScheduleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static dev.dunglv202.hoaithuong.model.criteria.ScheduleCriteria.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScheduleServiceImpl implements ScheduleService {
    private final ScheduleRepository scheduleRepository;
    private final TutorClassRepository tutorClassRepository;
    private final AuthHelper authHelper;
    private final CalendarService calendarService;
    private final LectureRepository lectureRepository;
    private final NotificationService notificationService;

    @Override
    public List<MinimalScheduleDTO> getSchedules(Range<LocalDate> range) {
        User signedUser = authHelper.getSignedUser();
        return scheduleRepository.findAll(ScheduleCriteria.ofTeacher(signedUser).and(joinFetch()).and(inRange(range)))
            .stream()
            .map(ScheduleMapper.INSTANCE::toMinimalScheduleDTO)
            .toList();
    }

    @Override
    @Transactional
    public void deleteSchedule(Long id) {
        Schedule scheduleToDelete = scheduleRepository.findByIdAndTeacher(id, authHelper.getSignedUser())
            .orElseThrow();

        // delete
        TutorClass tutorClass = scheduleToDelete.getTutorClass();
        Schedule lastSchedule = scheduleRepository.findLastByTutorClass(tutorClass);
        deleteSchedule(scheduleToDelete);

        // add new schedule after last schedule
        List<Schedule> replacement = new ScheduleGenerator()
            .setClass(tutorClass)
            .setStartTime(lastSchedule.getEndTime())
            .generate(1);
        addSchedules(replacement);
    }

    /**
     * Add new class to schedule if that class have timeslots
     */
    @Override
    @Transactional
    public void addSchedulesForClass(TutorClass newClass, LocalDate startDate) {
        List<Schedule> schedules = new ScheduleGenerator()
            .setClass(newClass)
            .setStartTime(startDate.atStartOfDay())
            .generate(newClass.getTotalLecture() - newClass.getLearned());
        addSchedules(schedules);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public Schedule addSingleScheduleForClass(TutorClass tutorClass, LocalDateTime startTime) {
        // remove last lecture
        Schedule lastSchedule = scheduleRepository.findLastByTutorClass(tutorClass);
        deleteSchedule(lastSchedule);

        // make schedule & try to add
        Schedule schedule = makeSchedule(tutorClass, startTime);
        addSchedules(List.of(schedule));

        return schedule;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void updateScheduleForClass(TutorClass tutorClass, LocalDate startDate, Set<TimeSlot> timeSlots) {
        var oldSchedules = scheduleRepository.findAll(ofClass(tutorClass).and(inRange(SimpleRange.from(startDate))));
        deleteSchedules(oldSchedules);
        tutorClass.setTimeSlots(new ArrayList<>(timeSlots));
        addSchedulesForClass(tutorClass, startDate);
    }

    @Override
    @Transactional
    public void addNewSchedule(NewScheduleDTO newSchedule) {
        TutorClass tutorClass = tutorClassRepository.findByIdAndTeacher(newSchedule.getClassId(), authHelper.getSignedUser())
            .orElseThrow(() -> new ClientVisibleException("{tutor_class.not_found}"));
        addSingleScheduleForClass(tutorClass, newSchedule.getStartTime());
    }

    /**
     * Sync schedule in {@code range} to Google Calendar for current signed user
     */
    @Override
    public void syncToCalendar(Range<LocalDate> range) {
        // sync to Calendar
        var signedUser = authHelper.getSignedUser();
        var spec = Specification.allOf(
            ofTeacher(signedUser),
            inRange(range),
            synced(false)
        );
        var notSynced = scheduleRepository.findAll(joinFetch().and(spec));
        calendarService.addEvents(signedUser, notSynced.stream().map(ScheduleEvent::new).toList());
        scheduleRepository.saveAll(notSynced);
    }

    /**
     * All schedule delete action should call this method
     */
    @Override
    public void deleteSchedules(List<Schedule> schedules) {
        if (schedules.isEmpty()) return;
        calendarService.removeEventsAsync(
            schedules.get(0).getTeacher(),
            schedules.stream().filter(s -> s.getGoogleEventId() != null).map(ScheduleEvent::new).toList()
        );
        scheduleRepository.deleteAll(schedules);
    }

    /**
     * Sync schedule since last lecture to new calendar
     */
    @Async
    @Override
    public void syncToNewCalendarAsync(User teacher) {
        try {
            Optional<Lecture> lastLecture = lectureRepository.findLatestByTeacher(teacher);
            if (lastLecture.isEmpty()) return;

            // sync user since last lecture
            List<Schedule> schedules = scheduleRepository.findAllByTeacherAndAfter(
                teacher,
                lastLecture.get().getSchedule().getStartTime()
            );
            calendarService.addEvents(teacher, schedules.stream().map(ScheduleEvent::new).toList());
        } catch (Exception e) {
            log.error("Could not sync to new calendar for user id {} ", teacher.getId(), e);
            Notification notification = Notification.forUser(teacher)
                .content("{sync_to_new_calendar.failed}");
            notificationService.addNotification(notification);
        }
    }

    /**
     * Add schedule to timetable. Required to be in a transaction. All schedule add action should call this method
     *
     * @param schedules List of schedule to add, sorted in ascending order by date time
     */
    private void addSchedules(List<Schedule> schedules) {
        if (schedules.isEmpty()) return;

        // check for conflicts with other schedules
        Schedule firstSchedule = schedules.get(0);
        Schedule lastSchedule = schedules.get(schedules.size() - 1);
        List<Schedule> activeSchedules = scheduleRepository.findAllInRangeByTeacher(
            firstSchedule.getTeacher(),
            firstSchedule.getStartTime().toLocalDate(),
            lastSchedule.getEndTime().toLocalDate()
        );
        for (Schedule schedule : schedules) {
            for (Schedule scheduled : activeSchedules) {
                if (scheduled.isAfter(schedule)) break;
                if (schedule.overlaps(scheduled)) {
                    throw new ConflictScheduleException(scheduled);
                }
            }
        }

        // no conflicts => save schedule
        scheduleRepository.saveAll(schedules);
        calendarService.addEventsAsync(schedules.get(0).getTeacher(), schedules.stream().map(ScheduleEvent::new).toList());
    }

    private void deleteSchedule(Schedule schedule) {
        deleteSchedules(List.of(schedule));
    }

    private Schedule makeSchedule(TutorClass tutorClass, LocalDateTime startTime) {
        Schedule schedule = new Schedule();
        schedule.setStartTime(startTime);
        schedule.setEndTime(schedule.getStartTime().plus(tutorClass.getDuration()));
        schedule.setTutorClass(tutorClass);

        return schedule;
    }
}
