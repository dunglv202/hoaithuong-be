package dev.dunglv202.hoaithuong.model;

import dev.dunglv202.hoaithuong.entity.Student_;
import dev.dunglv202.hoaithuong.entity.TutorClass;
import dev.dunglv202.hoaithuong.entity.TutorClass_;
import jakarta.persistence.criteria.Predicate;
import lombok.Setter;
import org.springframework.data.jpa.domain.Specification;

@Setter
public class TutorClassCriteria {
    private String keyword;

    public static Specification<TutorClass> containsKeyword(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return Specification.where(null);
        }

        return (root, query, cb) -> {
            String pattern = "%" + keyword + "%";
            Predicate studentContainsKeyword = cb.like(root.get(TutorClass_.STUDENT).get(Student_.NAME), pattern);
            Predicate codeContainsKeyword = cb.like(root.get(TutorClass_.CODE), pattern);

            return cb.or(studentContainsKeyword, codeContainsKeyword);
        };
    }

    public Specification<TutorClass> toSpecification() {
        return containsKeyword(keyword);
    }
}
