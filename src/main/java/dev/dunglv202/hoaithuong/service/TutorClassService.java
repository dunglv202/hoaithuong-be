package dev.dunglv202.hoaithuong.service;

import dev.dunglv202.hoaithuong.dto.NewTutorClassDTO;
import dev.dunglv202.hoaithuong.dto.TutorClassDTO;
import dev.dunglv202.hoaithuong.entity.Schedule;
import dev.dunglv202.hoaithuong.entity.Student;
import dev.dunglv202.hoaithuong.entity.TutorClass;
import dev.dunglv202.hoaithuong.entity.User;
import dev.dunglv202.hoaithuong.exception.ClientVisibleException;
import dev.dunglv202.hoaithuong.exception.ConflictScheduleException;
import dev.dunglv202.hoaithuong.helper.AuthHelper;
import dev.dunglv202.hoaithuong.model.TimeSlot;
import dev.dunglv202.hoaithuong.model.TutorClassCriteria;
import dev.dunglv202.hoaithuong.repository.ScheduleRepository;
import dev.dunglv202.hoaithuong.repository.StudentRepository;
import dev.dunglv202.hoaithuong.repository.TutorClassRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static dev.dunglv202.hoaithuong.model.TutorClassCriteria.hasActiveStatus;
import static dev.dunglv202.hoaithuong.model.TutorClassCriteria.ofTeacher;

@Service
@RequiredArgsConstructor
public class TutorClassService {
    private final TutorClassRepository tutorClassRepository;
    private final StudentRepository studentRepository;
    private final AuthHelper authHelper;
    private final ScheduleRepository scheduleRepository;

    @Transactional
    public void addNewClass(NewTutorClassDTO newTutorClassDTO) {
        TutorClass tutorClass = newTutorClassDTO.toEntity();

        Student student = studentRepository.findById(newTutorClassDTO.getStudentId())
            .orElseThrow(() -> new ClientVisibleException("{student.not_found}"));
        tutorClass.setStudent(student);
        
        if (tutorClassRepository.existsByCode(newTutorClassDTO.getCode())) {
            throw new ClientVisibleException("{tutor_class.code.existed}");
        }

        // set end time for time slots before save & check for conflict
        tutorClass.getTimeSlots().forEach(t -> t.setEndTime(t.getStartTime().plus(tutorClass.getDuration())));
        addToTeacherSchedule(authHelper.getSignedUser(), tutorClass);

        tutorClassRepository.save(tutorClass);
    }

    /**
     * Add new class schedule to teacher schedule
     *
     * @throws ConflictScheduleException Schedule conflicts with other classes
     */
    private void addToTeacherSchedule(User teacher, TutorClass newClass) {
        // check for conflicts
        for (TimeSlot timeSlot : newClass.getTimeSlots()) {
            List<TimeSlot> currentTimeSlots = getActiveTimeSlotsForTeacher(teacher);

            if (overlapsWithOtherTimeSlots(timeSlot, currentTimeSlots)) {
                throw new ConflictScheduleException();
            }
        }

        // sort timeslots in ascending order
        Collections.sort(newClass.getTimeSlots());

        // add to schedule
        List<Schedule> schedules = new ArrayList<>();
        int numOfLectures = newClass.getTotalLecture() - newClass.getLearned();
        LocalDate date = newClass.getStartDate() != null
            ? newClass.getStartDate()
            : Instant.now().atZone(LocaleContextHolder.getTimeZone().toZoneId()).toLocalDate();
        // loop over days until no lecture remain
        while (numOfLectures > 0) {
            // get time slots for today -> if available -> make schedule
            DayOfWeek today = date.getDayOfWeek();
            List<TimeSlot> todaySlots = newClass.getTimeSlots()
                .stream()
                .filter(s -> s.getWeekday() == today)
                .toList();
            for (TimeSlot slot : todaySlots) {
                if (numOfLectures <= 0) break;
                schedules.add(makeScheduleForSlot(newClass, date, slot));
                numOfLectures--;
            }

            // go to next day
            date = date.plusDays(1);
        }
        scheduleRepository.saveAll(schedules);
    }

    /**
     * Make schedule instance for class and date with time slot
     */
    private Schedule makeScheduleForSlot(TutorClass tutorClass, LocalDate date, TimeSlot slot) {
        Schedule schedule = new Schedule();
        schedule.setTutorClass(tutorClass);
        schedule.setStartTime(date.atTime(slot.getStartTime()));
        schedule.setEndTime(date.atTime(slot.getEndTime()));
        return schedule;
    }

    /**
     * TODO: Add from day
     *
     * @return List of time slots that teacher is having in their schedule
     */
    private List<TimeSlot> getActiveTimeSlotsForTeacher(User teacher) {
        List<TutorClass> activeClasses = tutorClassRepository.findAll(ofTeacher(teacher).and(hasActiveStatus(true)));

        return activeClasses.stream()
            .flatMap(tutorClass -> tutorClass.getTimeSlots().stream())
            .toList();
    }

    private boolean overlapsWithOtherTimeSlots(TimeSlot newTimeSlot, List<TimeSlot> otherTimeSlots) {
        for (TimeSlot timeSlot : otherTimeSlots) {
            if (newTimeSlot.overlaps(timeSlot)) return true;
        }
        return false;
    }

    public List<TutorClassDTO> getAllClasses(TutorClassCriteria criteria) {
        return tutorClassRepository.findAll(criteria.toSpecification())
            .stream()
            .map(TutorClassDTO::new)
            .toList();
    }
}
