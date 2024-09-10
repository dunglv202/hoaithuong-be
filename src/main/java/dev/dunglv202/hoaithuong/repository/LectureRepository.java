package dev.dunglv202.hoaithuong.repository;

import dev.dunglv202.hoaithuong.entity.Lecture;
import dev.dunglv202.hoaithuong.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface LectureRepository extends JpaRepository<Lecture, Long>, JpaSpecificationExecutor<Lecture> {
    Optional<Lecture> findByIdAndTeacher(long id, User teacher);
}
