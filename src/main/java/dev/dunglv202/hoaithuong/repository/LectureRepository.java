package dev.dunglv202.hoaithuong.repository;

import dev.dunglv202.hoaithuong.entity.Lecture;
import dev.dunglv202.hoaithuong.entity.TutorClass;
import dev.dunglv202.hoaithuong.model.ReportRange;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface LectureRepository extends JpaRepository<Lecture, Long> {
    @Query("""
        FROM Lecture l
        WHERE (:#{#range.startTime} IS NULL OR l.startTime >= :#{#range.startTime})
            AND (:#{#range.endTime} IS NULL OR l.startTime <= :#{#range.endTime})
        ORDER BY l.startTime ASC
    """)
    List<Lecture> findAllInRange(@Param("range") ReportRange range);

    @Query("""
        FROM Lecture l
        WHERE l.tutorClass = :tutorClass
        AND l.startTime >= :from
        ORDER BY l.lectureNo ASC
    """)
    List<Lecture> findAllByClassIdFromTime(
        @Param("tutorClass") TutorClass tutorClass,
        @Param("from") Instant from
    );
}
