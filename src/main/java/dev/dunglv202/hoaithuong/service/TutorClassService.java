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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
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
        addToSchedule(authHelper.getSignedUser(), tutorClass);

        tutorClassRepository.save(tutorClass);
    }

    /**
     * Add new class schedule to teacher schedule
     *
     * @throws ConflictScheduleException Schedule conflicts with other classes
     */
    private void addToSchedule(User teacher, TutorClass newTutorClass) {
        // check for conflicts
        for (TimeSlot timeSlot : newTutorClass.getTimeSlots()) {
            List<TimeSlot> currentTimeSlots = getActiveTimeSlotsForTeacher(teacher);

            if (overlapsWithOtherTimeSlots(timeSlot, currentTimeSlots)) {
                throw new ConflictScheduleException();
            }
        }

        // add to schedule
        List<Schedule> schedules = new ArrayList<>();
        for (int i = 1; i <= newTutorClass.getTotalLecture(); i++) {
            Schedule schedule = new Schedule();
            schedule.setTutorClass(newTutorClass);
            /* @TODO: calculate start time & move to next time slot */
            LocalDateTime startTime = LocalDateTime.now();
            schedule.setStartTime(startTime);
            schedule.setEndTime(startTime.plus(newTutorClass.getDuration()));

            schedules.add(schedule);
        }
        scheduleRepository.saveAll(schedules);
    }

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
