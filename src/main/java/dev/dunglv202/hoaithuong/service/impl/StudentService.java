package dev.dunglv202.hoaithuong.service.impl;

import dev.dunglv202.hoaithuong.dto.NewStudentDTO;
import dev.dunglv202.hoaithuong.dto.StudentDTO;
import dev.dunglv202.hoaithuong.entity.Student;
import dev.dunglv202.hoaithuong.mapper.StudentMapper;
import dev.dunglv202.hoaithuong.model.Page;
import dev.dunglv202.hoaithuong.model.Pagination;
import dev.dunglv202.hoaithuong.model.StudentCriteria;
import dev.dunglv202.hoaithuong.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StudentService {
    private final StudentRepository studentRepository;

    public void addNewStudent(NewStudentDTO newStudentDTO) {
        Student student = StudentMapper.INSTANCE.toStudent(newStudentDTO);
        studentRepository.save(student);
    }

    public Page<StudentDTO> getAllStudents(StudentCriteria criteria, Pagination pagination) {
        return new Page<>(
            studentRepository.findAll(criteria.toSpecification(), pagination.pageable())
                .map(StudentMapper.INSTANCE::toStudentDTO)
        );
    }
}
