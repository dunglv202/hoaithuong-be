package dev.dunglv202.hoaithuong.service;

import dev.dunglv202.hoaithuong.dto.NewStudentDTO;
import dev.dunglv202.hoaithuong.dto.StudentDTO;
import dev.dunglv202.hoaithuong.entity.Student;
import dev.dunglv202.hoaithuong.model.StudentCriteria;
import dev.dunglv202.hoaithuong.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StudentService {
    private final StudentRepository studentRepository;

    public void addNewStudent(NewStudentDTO newStudentDTO) {
        Student student = newStudentDTO.toEntity();
        studentRepository.save(student);
    }

    public List<StudentDTO> getAllStudents(StudentCriteria criteria) {
        return studentRepository.findAll(criteria.toSpecification())
            .stream()
            .map(StudentDTO::new)
            .toList();
    }
}
