package dev.dunglv202.hoaithuong.repository;

import dev.dunglv202.hoaithuong.entity.Lecture;
import dev.dunglv202.hoaithuong.model.ReportRange;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

public interface LectureRepository extends JpaRepository<Lecture, Long>, JpaSpecificationExecutor<Lecture> {
    @Query("""
        SELECT COALESCE(SUM(l.tutorClass.payForLecture), 0)
        FROM Lecture l
        WHERE (:#{#range.from} IS NULL OR CAST(l.schedule.startTime AS DATE) >= :#{#range.from})
            AND (:#{#range.to} IS NULL OR CAST(l.schedule.startTime AS DATE) <= :#{#range.to})
    """)
    int getTotalEarnedByRange(ReportRange range);
}
