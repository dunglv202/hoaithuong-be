package dev.dunglv202.hoaithuong.service;

import dev.dunglv202.hoaithuong.dto.NewTutorClassDTO;
import dev.dunglv202.hoaithuong.dto.TutorClassDTO;
import dev.dunglv202.hoaithuong.entity.Student;
import dev.dunglv202.hoaithuong.entity.TutorClass;
import dev.dunglv202.hoaithuong.entity.User;
import dev.dunglv202.hoaithuong.exception.ClientVisibleException;
import dev.dunglv202.hoaithuong.helper.AuthHelper;
import dev.dunglv202.hoaithuong.model.TimeSlot;
import dev.dunglv202.hoaithuong.model.TutorClassCriteria;
import dev.dunglv202.hoaithuong.repository.StudentRepository;
import dev.dunglv202.hoaithuong.repository.TutorClassRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static dev.dunglv202.hoaithuong.model.TutorClassCriteria.hasActiveStatus;

@Service
@RequiredArgsConstructor
public class TutorClassService {
    private final TutorClassRepository tutorClassRepository;
    private final StudentRepository studentRepository;
    private final AuthHelper authHelper;

    @Transactional
    public void addNewClass(NewTutorClassDTO newTutorClassDTO) {
        TutorClass tutorClass = newTutorClassDTO.toEntity();

        Student student = studentRepository.findById(newTutorClassDTO.getStudentId())
            .orElseThrow(() -> new ClientVisibleException("{student.not_found}"));
        tutorClass.setStudent(student);
        
        if (tutorClassRepository.existsByCode(newTutorClassDTO.getCode())) {
            throw new ClientVisibleException("{tutor_class.code.existed}");
        }

        addToSchedule(authHelper.getSignedUser(), newTutorClassDTO);

        tutorClassRepository.save(tutorClass);
    }

    private void addToSchedule(User teacher, NewTutorClassDTO newTutorClass) {

    }

    private boolean isLegalTimeSlot(User teacher, TimeSlot timeSlot) {
        List<TutorClass> activeClasses = tutorClassRepository.findAll(hasActiveStatus(true));
        List<TimeSlot> currentTimeSlots = activeClasses.stream()
            .flatMap(tutorClass -> tutorClass.getTimeSlots().stream())
            .toList();


    }

    public List<TutorClassDTO> getAllClasses(TutorClassCriteria criteria) {
        return tutorClassRepository.findAll(criteria.toSpecification())
            .stream()
            .map(TutorClassDTO::new)
            .toList();
    }
}
