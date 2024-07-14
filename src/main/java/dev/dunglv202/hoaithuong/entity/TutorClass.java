package dev.dunglv202.hoaithuong.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.Duration;

@Entity
@Data
public class TutorClass {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String code;

    @ManyToOne
    private Student student;

    private String level;

    private String notes;

    private int totalLecture;

    private int learned;

    private int durationInMinute;

    private int payForLecture;

    private boolean active;

    @PrePersist
    public void prePersist() {
        active = learned < totalLecture;
    }

    @PreUpdate
    public void preUpdate() {
        active = learned < totalLecture;
    }

    public Duration getDuration() {
        return Duration.ofMinutes(durationInMinute);
    }
}
