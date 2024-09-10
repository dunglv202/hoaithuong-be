package dev.dunglv202.hoaithuong.repository;

import dev.dunglv202.hoaithuong.entity.Student;
import dev.dunglv202.hoaithuong.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, Long>, JpaSpecificationExecutor<Student> {
    Optional<Student> findByIdAndCreatedBy(Long id, User createdBy);
}
