package dev.dunglv202.hoaithuong.repository;

import dev.dunglv202.hoaithuong.entity.Lecture;
import dev.dunglv202.hoaithuong.model.ReportRange;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LectureRepository extends JpaRepository<Lecture, Long> {
    @Query("""
        FROM Lecture l
        WHERE (:#{#range.startDate} IS NULL OR DATE(l.startTime) >= :#{#range.startDate})
            AND (:#{#range.endDate} IS NULL OR DATE(l.startTime) <= :#{#range.endDate})
        ORDER BY l.startTime ASC
    """)
    List<Lecture> findAllInRange(@Param("range") ReportRange range);
}
