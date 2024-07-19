package dev.dunglv202.hoaithuong.controller;

import dev.dunglv202.hoaithuong.dto.NewStudentDTO;
import dev.dunglv202.hoaithuong.dto.StudentDTO;
import dev.dunglv202.hoaithuong.model.StudentCriteria;
import dev.dunglv202.hoaithuong.service.impl.StudentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/students")
@RequiredArgsConstructor
public class StudentController {
    private final StudentService studentService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public void addNewStudent(@Valid @RequestBody NewStudentDTO newStudentDTO) {
        studentService.addNewStudent(newStudentDTO);
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public List<StudentDTO> getAllStudents(StudentCriteria criteria) {
        return studentService.getAllStudents(criteria);
    }
}
