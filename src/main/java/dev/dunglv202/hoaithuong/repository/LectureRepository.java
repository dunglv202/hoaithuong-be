package dev.dunglv202.hoaithuong.repository;

import dev.dunglv202.hoaithuong.entity.Lecture;
import dev.dunglv202.hoaithuong.entity.Student;
import dev.dunglv202.hoaithuong.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface LectureRepository extends JpaRepository<Lecture, Long>, JpaSpecificationExecutor<Lecture> {
    Optional<Lecture> findByIdAndTeacher(long id, User teacher);

    @Query("""
        FROM Lecture l
        JOIN FETCH l.tutorClass
        WHERE l.teacher = :teacher
        AND l.tutorClass.student = :student
        AND MONTH(l.schedule.startTime) = :month AND YEAR(l.schedule.startTime) = :year
    """)
    List<Lecture> findByTimeAndStudentForTeacher(
        @Param("year") int year,
        @Param("month") int month,
        @Param("teacher") User teacher,
        @Param("student") Student student
    );
}
