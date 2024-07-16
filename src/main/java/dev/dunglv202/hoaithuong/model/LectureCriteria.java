package dev.dunglv202.hoaithuong.model;

import dev.dunglv202.hoaithuong.entity.Lecture;
import dev.dunglv202.hoaithuong.entity.Lecture_;
import dev.dunglv202.hoaithuong.entity.TutorClass;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;

public class LectureCriteria {
    public static Specification<Lecture> from(Instant from) {
        if (from == null) return Specification.where(null);

        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get(Lecture_.START_TIME), from);
    }

    public static Specification<Lecture> to(Instant to) {
        if (to == null) return Specification.where(null);

        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get(Lecture_.START_TIME), to);
    }

    public static Specification<Lecture> inRange(Range<Instant> range) {
        if (range == null) return Specification.where(null);

        return from(range.getFrom()).and(to(range.getTo()));
    }

    public static Specification<Lecture> ofClass(TutorClass tutorClass) {
        if (tutorClass == null) return Specification.where(null);

        return (root, query, cb) -> cb.equal(root.get(Lecture_.TUTOR_CLASS), tutorClass);
    }
}
