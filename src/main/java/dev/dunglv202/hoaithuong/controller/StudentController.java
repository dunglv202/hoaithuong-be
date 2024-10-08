package dev.dunglv202.hoaithuong.controller;

import dev.dunglv202.hoaithuong.dto.NewStudentDTO;
import dev.dunglv202.hoaithuong.dto.StudentDTO;
import dev.dunglv202.hoaithuong.dto.UpdatedStudentDTO;
import dev.dunglv202.hoaithuong.model.Page;
import dev.dunglv202.hoaithuong.model.Pagination;
import dev.dunglv202.hoaithuong.model.criteria.StudentCriteria;
import dev.dunglv202.hoaithuong.service.impl.StudentServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/students")
@RequiredArgsConstructor
public class StudentController {
    private final StudentServiceImpl studentService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public void addNewStudent(@Valid @RequestBody NewStudentDTO newStudentDTO) {
        studentService.addNewStudent(newStudentDTO);
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public Page<StudentDTO> getAllStudents(StudentCriteria criteria, Pagination pagination) {
        return studentService.getAllStudents(criteria, pagination.limit(20));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public void updateStudent(@Valid @RequestBody UpdatedStudentDTO updated, @PathVariable Long id) {
        updated.setId(id);
        studentService.updateStudent(updated);
    }
}
