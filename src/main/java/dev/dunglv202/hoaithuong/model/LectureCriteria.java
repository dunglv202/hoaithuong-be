package dev.dunglv202.hoaithuong.model;

import dev.dunglv202.hoaithuong.entity.*;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Path;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class LectureCriteria {
    public static Specification<Lecture> joinFetch() {
        return (root, query, cb) -> {
            root.fetch(Lecture_.tutorClass).fetch(TutorClass_.student);
            root.fetch(Lecture_.schedule);
            return cb.conjunction();
        };
    }

    public static Specification<Lecture> from(LocalDateTime from) {
        if (from == null) return Specification.where(null);

        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get(Lecture_.SCHEDULE).get(Schedule_.START_TIME), from);
    }

    public static Specification<Lecture> fromDate(LocalDate from) {
        if (from == null) return Specification.where(null);

        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get(Lecture_.SCHEDULE).get(Schedule_.START_TIME).as(LocalDate.class), from);
    }

    public static Specification<Lecture> toDate(LocalDate to) {
        if (to == null) return Specification.where(null);

        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get(Lecture_.SCHEDULE).get(Schedule_.START_TIME).as(LocalDate.class), to);
    }

    public static Specification<Lecture> inRange(Range<LocalDate> range) {
        if (range == null) return Specification.where(null);

        return fromDate(range.getFrom()).and(toDate(range.getTo()));
    }

    public static Specification<Lecture> ofClass(TutorClass tutorClass) {
        if (tutorClass == null) return Specification.where(null);

        return (root, query, cb) -> cb.equal(root.get(Lecture_.TUTOR_CLASS), tutorClass);
    }

    public static Specification<Lecture> sortByStartTime(Sort.Direction direction) {
        return (root, query, cb) -> {
            Path<Lecture> startTimeAttr = root.get(Lecture_.SCHEDULE).get(Schedule_.START_TIME);
            Order order = direction == Sort.Direction.DESC
                ? cb.desc(startTimeAttr)
                : cb.asc(startTimeAttr);
            query.orderBy(order);
            return cb.conjunction();
        };
    }
}
