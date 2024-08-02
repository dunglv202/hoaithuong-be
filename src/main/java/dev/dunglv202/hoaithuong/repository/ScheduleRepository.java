package dev.dunglv202.hoaithuong.repository;

import dev.dunglv202.hoaithuong.entity.Schedule;
import dev.dunglv202.hoaithuong.entity.TutorClass;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface ScheduleRepository extends JpaRepository<Schedule, Long>, JpaSpecificationExecutor<Schedule> {
    @Query("""
        FROM Schedule s
        WHERE CAST(s.startTime AS DATE) BETWEEN :from AND :to
        ORDER BY s.startTime ASC
    """)
    List<Schedule> findAllInRange(@Param("from") LocalDate from, @Param("to") LocalDate to);

    @Query("""
        FROM Schedule s
        WHERE CAST(s.startTime AS DATE) = :date
    """)
    List<Schedule> findAllInDate(@Param("date") LocalDate date);

    int countByTutorClass(TutorClass tutorClass);

    @Query("""
        FROM Schedule c
        WHERE c.tutorClass = :tutorClass
        ORDER BY c.startTime DESC
        LIMIT 1
    """)
    Schedule findLastByTutorClass(@Param("tutorClass") TutorClass tutorClass);

    @Modifying
    @Query("""
        DELETE FROM Schedule s
        WHERE s.tutorClass = :tutorClass AND CAST(s.startTime AS DATE) >= :startDate
    """)
    void deleteAllFromDateByClass(@Param("tutorClass") TutorClass tutorClass, @Param("startDate") LocalDate startDate);
}
