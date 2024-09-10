package dev.dunglv202.hoaithuong.service;

import dev.dunglv202.hoaithuong.dto.NewStudentDTO;
import dev.dunglv202.hoaithuong.dto.StudentDTO;
import dev.dunglv202.hoaithuong.dto.UpdatedStudentDTO;
import dev.dunglv202.hoaithuong.model.Page;
import dev.dunglv202.hoaithuong.model.Pagination;
import dev.dunglv202.hoaithuong.model.criteria.StudentCriteria;

public interface StudentService {
    void addNewStudent(NewStudentDTO newStudentDTO);

    Page<StudentDTO> getAllStudents(StudentCriteria criteria, Pagination pagination);

    void updateStudent(UpdatedStudentDTO updated);
}
