package dev.dunglv202.hoaithuong.model;

import dev.dunglv202.hoaithuong.entity.Lecture;
import dev.dunglv202.hoaithuong.entity.Lecture_;
import dev.dunglv202.hoaithuong.entity.TutorClass;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class LectureCriteria {
    public static Specification<Lecture> from(LocalDateTime from) {
        if (from == null) return Specification.where(null);

        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get(Lecture_.START_TIME), from);
    }

    public static Specification<Lecture> fromDate(LocalDate from) {
        if (from == null) return Specification.where(null);

        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get(Lecture_.START_TIME).as(LocalDate.class), from);
    }

    public static Specification<Lecture> toDate(LocalDate to) {
        if (to == null) return Specification.where(null);

        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get(Lecture_.START_TIME).as(LocalDate.class), to);
    }

    public static Specification<Lecture> inRange(Range<LocalDate> range) {
        if (range == null) return Specification.where(null);

        return fromDate(range.getFrom()).and(toDate(range.getTo()));
    }

    public static Specification<Lecture> ofClass(TutorClass tutorClass) {
        if (tutorClass == null) return Specification.where(null);

        return (root, query, cb) -> cb.equal(root.get(Lecture_.TUTOR_CLASS), tutorClass);
    }
}
