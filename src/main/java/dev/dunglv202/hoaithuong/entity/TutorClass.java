package dev.dunglv202.hoaithuong.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.Duration;

@Entity
@Data
public class TutorClass extends BaseEntity {
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

    @Override
    public void prePersist() {
        super.prePersist();
        active = learned < totalLecture;
    }

    @Override
    public void preUpdate() {
        super.preUpdate();
        active = learned < totalLecture;
    }

    public Duration getDuration() {
        return Duration.ofMinutes(durationInMinute);
    }
}
