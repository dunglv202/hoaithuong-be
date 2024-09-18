package dev.dunglv202.hoaithuong.repository;

import dev.dunglv202.hoaithuong.entity.Schedule;
import dev.dunglv202.hoaithuong.entity.TutorClass;
import dev.dunglv202.hoaithuong.entity.User;
import dev.dunglv202.hoaithuong.service.ScheduleService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
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

    @Query("""
        SELECT COALESCE(SUM(s.tutorClass.payForLecture), 0)
        FROM Schedule s
        WHERE s.teacher = :teacher AND CAST(s.startTime AS DATE) BETWEEN :from AND :to
    """)
    int getEstimatedTotalInRange(@Param("teacher") User teacher, @Param("from") LocalDate from, @Param("to") LocalDate to);
}
