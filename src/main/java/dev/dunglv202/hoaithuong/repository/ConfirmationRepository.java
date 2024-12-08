package dev.dunglv202.hoaithuong.repository;

import dev.dunglv202.hoaithuong.entity.Confirmation;
import dev.dunglv202.hoaithuong.entity.Report;
import dev.dunglv202.hoaithuong.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ConfirmationRepository extends JpaRepository<Confirmation, Long> {
    Optional<Confirmation> findByReportAndStudent(Report report, Student student);

    List<Confirmation> findAllByReport(Report report);
}
