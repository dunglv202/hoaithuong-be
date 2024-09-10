package dev.dunglv202.hoaithuong.repository;

import dev.dunglv202.hoaithuong.entity.TutorClass;
import dev.dunglv202.hoaithuong.entity.User;
import jakarta.annotation.Nonnull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface TutorClassRepository extends JpaRepository<TutorClass, Long>, JpaSpecificationExecutor<TutorClass> {
    Optional<TutorClass> findByIdAndTeacher(Long id, User teacher);

    boolean existsByCode(String code);

    @Override
    @Nonnull
    @EntityGraph(value = "TutorClass.student")
    Page<TutorClass> findAll(@Nonnull Specification<TutorClass> spec, @Nonnull Pageable pageable);
}
