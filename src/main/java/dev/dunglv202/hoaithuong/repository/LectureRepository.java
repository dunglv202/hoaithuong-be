package dev.dunglv202.hoaithuong.repository;

import dev.dunglv202.hoaithuong.entity.Lecture;
import dev.dunglv202.hoaithuong.model.ReportRange;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

public interface LectureRepository extends JpaRepository<Lecture, Long>, JpaSpecificationExecutor<Lecture> {
    @Query("""
        SELECT SUM(l.tutorClass.payForLecture)
        FROM Lecture l
        WHERE (:#{#range.from} IS NULL OR l.startTime >= :#{#range.from})
            AND (:#{#range.to} IS NULL OR l.startTime <= :#{#range.to})
    """)
    int getTotalEarnedByRange(ReportRange range);
}
