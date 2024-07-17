package dev.dunglv202.hoaithuong.repository;

import dev.dunglv202.hoaithuong.entity.Schedule;
import dev.dunglv202.hoaithuong.model.Range;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {
    @Query("""
        FROM Schedule s
        WHERE CAST(s.startTime AS DATE) BETWEEN :#{#range.from} AND :#{#range.to}
        ORDER BY s.startTime ASC
    """)
    List<Schedule> findAllInRange(Range<LocalDate> range);
}
