package dev.dunglv202.hoaithuong.model.criteria;

import dev.dunglv202.hoaithuong.entity.Student_;
import dev.dunglv202.hoaithuong.entity.TutorClass;
import dev.dunglv202.hoaithuong.entity.TutorClass_;
import dev.dunglv202.hoaithuong.entity.User;
import jakarta.annotation.Nullable;
import jakarta.persistence.criteria.Predicate;
import lombok.Setter;
import org.springframework.data.jpa.domain.Specification;

@Setter
public class TutorClassCriteria {
    private String keyword;
    private Boolean active;

    public static Specification<TutorClass> containsKeyword(@Nullable String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return Specification.where(null);
        }

        return (root, query, cb) -> {
            String pattern = "%" + keyword + "%";
            Predicate studentContainsKeyword = cb.like(root.get(TutorClass_.student).get(Student_.name), pattern);
            Predicate codeContainsKeyword = cb.like(root.get(TutorClass_.code), pattern);

            return cb.or(studentContainsKeyword, codeContainsKeyword);
        };
    }

    public static Specification<TutorClass> hasActiveStatus(@Nullable Boolean active) {
        if (active == null) return Specification.where(null);
        return (root, query, cb) -> cb.equal(root.get(TutorClass_.active), active);
    }

    public static Specification<TutorClass> ofTeacher(User teacher) {
        return (root, query, cb) -> cb.equal(root.get(TutorClass_.teacher), teacher);
    }

    public Specification<TutorClass> toSpecification() {
        return hasActiveStatus(active).and(containsKeyword(keyword));
    }
}
