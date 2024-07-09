package dev.dunglv202.hoaithuong.service;

import dev.dunglv202.hoaithuong.dto.NewTutorClassDTO;
import dev.dunglv202.hoaithuong.dto.TutorClassDTO;
import dev.dunglv202.hoaithuong.entity.Level;
import dev.dunglv202.hoaithuong.entity.Student;
import dev.dunglv202.hoaithuong.entity.TutorClass;
import dev.dunglv202.hoaithuong.exception.ClientVisibleException;
import dev.dunglv202.hoaithuong.model.TutorClassCriteria;
import dev.dunglv202.hoaithuong.repository.LevelRepository;
import dev.dunglv202.hoaithuong.repository.StudentRepository;
import dev.dunglv202.hoaithuong.repository.TutorClassRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TutorClassService {
    private final TutorClassRepository tutorClassRepository;
    private final LevelRepository levelRepository;
    private final StudentRepository studentRepository;

    public void addNewClass(NewTutorClassDTO newTutorClassDTO) {
        TutorClass tutorClass = newTutorClassDTO.toEntity();

        Level level = levelRepository.findByCode(newTutorClassDTO.getLevelCode())
            .orElseThrow(() -> new ClientVisibleException("{level.invalid}"));
        tutorClass.setLevel(level);

        Student student = studentRepository.findById(newTutorClassDTO.getStudentId())
            .orElseThrow(() -> new ClientVisibleException("{student.not_found}"));
        tutorClass.setStudent(student);
        
        if (tutorClassRepository.existsByCode(newTutorClassDTO.getCode())) {
            throw new ClientVisibleException("{tutor_class.code.existed}");
        }

        tutorClassRepository.save(tutorClass);
    }

    public List<TutorClassDTO> getAllClasses(TutorClassCriteria criteria) {
        return tutorClassRepository.findAll(criteria.toSpecification())
            .stream()
            .map(TutorClassDTO::new)
            .toList();
    }
}
