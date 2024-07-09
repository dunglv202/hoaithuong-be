package dev.dunglv202.hoaithuong.model;

import dev.dunglv202.hoaithuong.entity.Student;
import dev.dunglv202.hoaithuong.entity.Student_;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.jpa.domain.Specification;

@Setter
@Getter
public class StudentCriteria {
    private String keyword;

    public static Specification<Student> containsKeyword(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return Specification.where(null);
        }

        return (root, query, cb) -> cb.like(root.get(Student_.NAME), "%" + keyword + "%");
    }

    public Specification<Student> toSpecification() {
        return containsKeyword(keyword);
    }
}
