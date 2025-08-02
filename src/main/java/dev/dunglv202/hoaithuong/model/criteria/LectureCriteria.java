package dev.dunglv202.hoaithuong.model.criteria;

import dev.dunglv202.hoaithuong.constant.TutorClassType;
import dev.dunglv202.hoaithuong.entity.*;
import dev.dunglv202.hoaithuong.model.Range;
import jakarta.annotation.Nullable;
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

    public static Specification<Lecture> from(@Nullable LocalDateTime from) {
        if (from == null) return Specification.where(null);

        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get(Lecture_.schedule).get(Schedule_.startTime), from);
    }

    public static Specification<Lecture> fromDate(@Nullable LocalDate from) {
        if (from == null) return Specification.where(null);
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get(Lecture_.schedule).get(Schedule_.startTime).as(LocalDate.class), from);
    }

    public static Specification<Lecture> toDate(@Nullable LocalDate to) {
        if (to == null) return Specification.where(null);
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get(Lecture_.schedule).get(Schedule_.startTime).as(LocalDate.class), to);
    }

    public static Specification<Lecture> inRange(Range<LocalDate> range) {
        if (range == null) return Specification.where(null);
        return fromDate(range.getFrom()).and(toDate(range.getTo()));
    }

    public static Specification<Lecture> ofClass(TutorClass tutorClass) {
        return (root, query, cb) -> cb.equal(root.get(Lecture_.tutorClass), tutorClass);
    }

    public static Specification<Lecture> ofTeacher(User teacher) {
        return (root, query, cb) -> cb.equal(root.get(Lecture_.teacher), teacher);
    }

    public static Specification<Lecture> ofClassType(TutorClassType type) {
        return (root, query, cb) -> cb.equal(root.get(Lecture_.tutorClass).get(TutorClass_.type), type);
    }

    public static Specification<Lecture> sortByStartTime(Sort.Direction direction) {
        return (root, query, cb) -> {
            Path<LocalDateTime> startTimeAttr = root.get(Lecture_.schedule).get(Schedule_.startTime);
            Order order = direction == Sort.Direction.DESC
                ? cb.desc(startTimeAttr)
                : cb.asc(startTimeAttr);
            query.orderBy(order);
            return cb.conjunction();
        };
    }
}
