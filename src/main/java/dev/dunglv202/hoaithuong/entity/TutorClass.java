package dev.dunglv202.hoaithuong.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class TutorClass {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String code;

    @ManyToOne
    private Student student;

    @ManyToOne
    private Level level;

    private String notes;

    private int totalLecture;

    private int learned;
}
