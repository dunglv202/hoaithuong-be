package dev.dunglv202.hoaithuong.model.criteria;

import dev.dunglv202.hoaithuong.entity.*;
import dev.dunglv202.hoaithuong.model.Range;
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

    public static Specification<Schedule> ofTeacher(User teacher) {
        return (root, query, cb) -> cb.equal(root.get(Schedule_.teacher), teacher);
    }

    public static Specification<Schedule> ofClass(TutorClass tutorClass) {
        return (root, query, cb) -> cb.equal(root.get(Schedule_.tutorClass), tutorClass);
    }

    public static Specification<Schedule> inRange(Range<LocalDate> range) {
        return (root, query, cb) -> {
            var from = range.getFrom() != null
                ? cb.greaterThanOrEqualTo(root.get(Schedule_.startTime).as(LocalDate.class), range.getFrom())
                : cb.conjunction();
            var to = range.getTo() != null
                ? cb.lessThanOrEqualTo(root.get(Schedule_.startTime).as(LocalDate.class), range.getTo())
                : cb.conjunction();
            return cb.and(from, to);
        };
    }

    public static Specification<Schedule> synced(boolean synced) {
        return (root, query, cb) -> synced
            ? cb.isNotNull(root.get(Schedule_.googleEventId))
            : cb.isNull(root.get(Schedule_.googleEventId));
    }
}
