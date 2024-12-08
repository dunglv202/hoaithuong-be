package dev.dunglv202.hoaithuong.repository;

import dev.dunglv202.hoaithuong.entity.Report;
import dev.dunglv202.hoaithuong.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ReportRepository extends JpaRepository<Report, Long> {
    @Query("""
        FROM Report r WHERE r.year = :year AND r.month = :month AND r.teacher = :teacher
    """)
    Optional<Report> findByTimeAndTeacher(@Param("year") int year, @Param("month") int month, @Param("teacher") User teacher);
}
