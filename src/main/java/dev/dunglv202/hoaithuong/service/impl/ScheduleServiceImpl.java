package dev.dunglv202.hoaithuong.service.impl;

import dev.dunglv202.hoaithuong.dto.MinimalScheduleDTO;
import dev.dunglv202.hoaithuong.dto.NewScheduleDTO;
import dev.dunglv202.hoaithuong.entity.Schedule;
import dev.dunglv202.hoaithuong.entity.TutorClass;
import dev.dunglv202.hoaithuong.entity.User;
import dev.dunglv202.hoaithuong.exception.ClientVisibleException;
import dev.dunglv202.hoaithuong.exception.ConflictScheduleException;
import dev.dunglv202.hoaithuong.helper.AuthHelper;
import dev.dunglv202.hoaithuong.helper.ScheduleGenerator;
import dev.dunglv202.hoaithuong.mapper.ScheduleMapper;
import dev.dunglv202.hoaithuong.model.Range;
import dev.dunglv202.hoaithuong.model.ScheduleEvent;
import dev.dunglv202.hoaithuong.model.TimeSlot;
import dev.dunglv202.hoaithuong.model.criteria.ScheduleCriteria;
import dev.dunglv202.hoaithuong.repository.ScheduleRepository;
import dev.dunglv202.hoaithuong.repository.TutorClassRepository;
import dev.dunglv202.hoaithuong.service.CalendarService;
import dev.dunglv202.hoaithuong.service.ScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static dev.dunglv202.hoaithuong.model.criteria.ScheduleCriteria.*;

@Service
@RequiredArgsConstructor
public class ScheduleServiceImpl implements ScheduleService {
    private final ScheduleRepository scheduleRepository;
    private final TutorClassRepository tutorClassRepository;
    private final AuthHelper authHelper;
    private final CalendarService calendarService;

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
        if (scheduleToDelete.getLecture() != null) throw new ClientVisibleException("{schedule.attached_to_lecture}");

        // delete
        TutorClass tutorClass = scheduleToDelete.getTutorClass();
        Schedule lastSchedule = scheduleRepository.findLastByTutorClass(tutorClass);
        scheduleRepository.delete(scheduleToDelete);

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
        Schedule lastLecture = scheduleRepository.findLastByTutorClass(tutorClass);
        scheduleRepository.delete(lastLecture);

        // make schedule & try to add
        Schedule schedule = makeSchedule(tutorClass, startTime);
        addSchedules(List.of(schedule));

        return schedule;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateScheduleForClass(TutorClass tutorClass, LocalDate startDate, Set<TimeSlot> timeSlots) {
        scheduleRepository.deleteAllFromDateByClass(tutorClass, startDate);
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
    @Transactional
    public void syncToCalendar(Range<LocalDate> range) {
        // sync to Calendar
        var signedUser = authHelper.getSignedUser();
        var spec = Specification.allOf(
            ofTeacher(signedUser),
            inRange(range),
            synced(false)
        );
        var notSynced = scheduleRepository.findAll(spec);
        calendarService.addEvents(signedUser, notSynced.stream().map(ScheduleEvent::new).toList());

        // save schedule with Google Calendar event id set
        scheduleRepository.saveAll(notSynced);
    }

    /**
     * Add schedule to timetable
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
    }

    private Schedule makeSchedule(TutorClass tutorClass, LocalDateTime startTime) {
        Schedule schedule = new Schedule();
        schedule.setStartTime(startTime);
        schedule.setEndTime(schedule.getStartTime().plus(tutorClass.getDuration()));
        schedule.setTutorClass(tutorClass);

        return schedule;
    }
}
