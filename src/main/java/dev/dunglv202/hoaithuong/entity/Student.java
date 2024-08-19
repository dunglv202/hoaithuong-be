package dev.dunglv202.hoaithuong.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Formula;

@Entity
@Getter
@Setter
public class Student extends Auditable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Formula("(SELECT COUNT(*) > 0 FROM tutor_class c WHERE c.student_id = id AND c.active = TRUE)")
    private boolean active;

    private String notes;
}
