package dev.dunglv202.hoaithuong.model;

import dev.dunglv202.hoaithuong.entity.Schedule;
import dev.dunglv202.hoaithuong.entity.Schedule_;
import dev.dunglv202.hoaithuong.entity.TutorClass_;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

public class ScheduleCriteria {
    public static Specification<Schedule> joinFetch() {
        return (root, query, cb) -> {
            root.fetch(Schedule_.lecture, JoinType.LEFT);
            root.fetch(Schedule_.tutorClass).fetch(TutorClass_.student);
            return cb.conjunction();
        };
    }

    public static Specification<Schedule> inRange(Range<LocalDate> range) {
        return (root, query, cb) -> cb.between(
            root.get(Schedule_.startTime).as(LocalDate.class),
            range.getFrom(),
            range.getTo()
        );
    }
}
