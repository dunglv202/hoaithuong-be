package dev.dunglv202.hoaithuong.repository;

import dev.dunglv202.hoaithuong.entity.Lecture;
import dev.dunglv202.hoaithuong.entity.Student;
import dev.dunglv202.hoaithuong.entity.User;
import dev.dunglv202.hoaithuong.model.Range;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface LectureRepository extends LectureCustomRepository, JpaRepository<Lecture, Long>, JpaSpecificationExecutor<Lecture> {
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

    @Query("""
        FROM Lecture l
        JOIN FETCH l.schedule
        WHERE l.teacher = :teacher
        AND (:#{#range.from} IS NULL OR DATE(l.schedule.startTime) >= :#{#range.from})
        AND (:#{#range.to} IS NULL OR DATE(l.schedule.startTime) <= :#{#range.to})
        AND l.video IS NULL
    """)
    List<Lecture> findAllNoVideoInRangeByTeacher(@Param("teacher") User teacher, @Param("range") Range<LocalDate> range);

    @Query("""
        FROM Lecture l
        WHERE l.tutorClass.code = :classCode AND l.lectureNo = :lectureNo
    """)
    Optional<Lecture> findByClassCodeAndLectureNo(
        @Param("classCode") String classCode,
        @Param("lectureNo") int lectureNo
    );
}
