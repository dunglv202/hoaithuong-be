package dev.dunglv202.hoaithuong.entity;

import dev.dunglv202.hoaithuong.dto.UpdatedTutorClassDTO;
import dev.dunglv202.hoaithuong.mapper.TutorClassMapper;
import dev.dunglv202.hoaithuong.model.TimeSlot;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Duration;
import java.util.List;

@Entity
@Getter
@Setter
@NamedEntityGraph(
    name = "TutorClass.student",
    attributeNodes = @NamedAttributeNode(TutorClass_.STUDENT)
)
public class TutorClass extends Auditable {
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

    private int initialLearned;

    private int durationInMinute;

    private int payForLecture;

    private boolean active;

    @JdbcTypeCode(SqlTypes.JSON)
    private List<TimeSlot> timeSlots;

    @PrePersist
    public void prePersist() {
        active = learned < totalLecture;
    }

    @PreUpdate
    public void preUpdate() {
        if (learned >= totalLecture) {
            active = false;
        }
    }

    public Duration getDuration() {
        return Duration.ofMinutes(durationInMinute);
    }

    public TutorClass merge(UpdatedTutorClassDTO updated) {
        TutorClassMapper.INSTANCE.mergeTutorClass(this, updated);
        return this;
    }
}
