package dev.dunglv202.hoaithuong.entity;

import jakarta.annotation.Nonnull;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class Schedule extends Auditable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private TutorClass tutorClass;

    @OneToOne(mappedBy = Lecture_.SCHEDULE)
    private Lecture lecture;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    public boolean overlaps(@Nonnull Schedule another) {
        return this.startTime.isBefore(another.startTime)
            ? this.endTime.isAfter(another.startTime)
            : this.startTime.isBefore(another.endTime);
    }

    public boolean isAfter(Schedule another) {
        return this.startTime.isAfter(another.endTime);
    }
}
