package dev.dunglv202.hoaithuong.repository;

import dev.dunglv202.hoaithuong.constant.TutorClassType;
import dev.dunglv202.hoaithuong.entity.Schedule;
import dev.dunglv202.hoaithuong.entity.TutorClass;
import dev.dunglv202.hoaithuong.entity.User;
import dev.dunglv202.hoaithuong.service.ScheduleService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Notes: schedule operation like add, delete might be performed via {@link ScheduleService} to ensure that it works correctly
 */
public interface ScheduleRepository extends JpaRepository<Schedule, Long>, JpaSpecificationExecutor<Schedule> {
    Optional<Schedule> findByIdAndTeacher(Long id, User teacher);

    @Query("""
        FROM Schedule s
        WHERE s.teacher = :teacher AND CAST(s.startTime AS DATE) BETWEEN :from AND :to
        ORDER BY s.startTime ASC
    """)
    List<Schedule> findAllInRangeByTeacher(@Param("teacher") User teacher, @Param("from") LocalDate from, @Param("to") LocalDate to);

    @Query("""
        FROM Schedule c
        WHERE c.tutorClass = :tutorClass
        ORDER BY c.startTime DESC
        LIMIT 1
    """)
    Schedule findLastByTutorClass(@Param("tutorClass") TutorClass tutorClass);

    /**
     * @return the estimated total since {@code from} to {@code to}, earning for that schedule will first be inferred
     * from the lecture, if lecture was not added to the schedule then sum the default earning from tutor class entity
     */
    @Query("""
        SELECT COALESCE(SUM(COALESCE(l.teacherEarning, c.payForLecture, 0)), 0)
        FROM Schedule s
        LEFT JOIN s.lecture l
        INNER JOIN s.tutorClass c
        WHERE s.teacher = :teacher
        AND (CAST(s.startTime AS DATE) BETWEEN :from AND :to)
        AND (:classType IS NULL OR s.tutorClass.type = :classType)
        AND (
            :keyword IS NULL
            OR s.tutorClass.code LIKE CONCAT(:keyword, '%')
            OR s.tutorClass.student.name LIKE CONCAT('%', :keyword, '%')
        )
    """)
    int getEstimatedTotalInRange(
        @Param("teacher") User teacher,
        @Param("from") LocalDate from,
        @Param("to") LocalDate to,
        @Param("classType") TutorClassType classType,
        @Param("keyword") String keyword
    );

    @Query("""
        FROM Schedule s
        WHERE s.teacher = :teacher
        AND s.startTime > :from
    """)
    List<Schedule> findAllByTeacherAndAfter(@Param("teacher") User teacher, @Param("from") LocalDateTime from);
}
