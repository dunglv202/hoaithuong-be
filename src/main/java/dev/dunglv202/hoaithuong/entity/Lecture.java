package dev.dunglv202.hoaithuong.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
public class Lecture {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private TutorClass tutorClass;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private String topic;

    private String notes;
}
