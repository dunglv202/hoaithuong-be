package dev.dunglv202.hoaithuong.service.impl;

import dev.dunglv202.hoaithuong.dto.NewStudentDTO;
import dev.dunglv202.hoaithuong.dto.StudentDTO;
import dev.dunglv202.hoaithuong.dto.UpdatedStudentDTO;
import dev.dunglv202.hoaithuong.entity.Student;
import dev.dunglv202.hoaithuong.entity.Student_;
import dev.dunglv202.hoaithuong.exception.ClientVisibleException;
import dev.dunglv202.hoaithuong.helper.AuthHelper;
import dev.dunglv202.hoaithuong.mapper.StudentMapper;
import dev.dunglv202.hoaithuong.model.Page;
import dev.dunglv202.hoaithuong.model.Pagination;
import dev.dunglv202.hoaithuong.model.criteria.StudentCriteria;
import dev.dunglv202.hoaithuong.repository.StudentRepository;
import dev.dunglv202.hoaithuong.service.StudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StudentServiceImpl implements StudentService {
    private final StudentRepository studentRepository;
    private final AuthHelper authHelper;

    @Override
    public void addNewStudent(NewStudentDTO newStudentDTO) {
        Student student = StudentMapper.INSTANCE.toStudent(newStudentDTO);
        studentRepository.save(student);
    }

    @Override
    public Page<StudentDTO> getAllStudents(StudentCriteria criteria, Pagination pagination) {
        Sort activeFirst = Sort.by(Sort.Direction.DESC, Student_.ACTIVE);
        Specification<Student> specification = StudentCriteria.ofTeacher(authHelper.getSignedUserRef())
            .and(criteria.toSpecification());
        return new Page<>(
            studentRepository.findAll(specification, pagination.withSort(activeFirst).pageable())
                .map(StudentMapper.INSTANCE::toStudentDTO)
        );
    }

    @Override
    public void updateStudent(UpdatedStudentDTO updated) {
        // only teacher that created this student can update
        Student student = studentRepository.findByIdAndCreatedBy(updated.getId(), authHelper.getSignedUserRef())
            .orElseThrow(() -> new ClientVisibleException("{student.not_found}"));
        studentRepository.save(student.merge(updated));
    }
}
