package dev.dunglv202.hoaithuong.repository;

import dev.dunglv202.hoaithuong.entity.TutorClass;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface TutorClassRepository extends JpaRepository<TutorClass, Long>, JpaSpecificationExecutor<TutorClass> {
    boolean existsByCode(String code);
}
