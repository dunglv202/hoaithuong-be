package dev.dunglv202.hoaithuong.model.criteria;

import dev.dunglv202.hoaithuong.entity.Student;
import dev.dunglv202.hoaithuong.entity.Student_;
import dev.dunglv202.hoaithuong.entity.User;
import jakarta.annotation.Nullable;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.jpa.domain.Specification;

@Setter
@Getter
public class StudentCriteria {
    private String keyword;

    public static Specification<Student> containsKeyword(@Nullable String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return Specification.where(null);
        }

        return (root, query, cb) -> cb.like(root.get(Student_.NAME), "%" + keyword + "%");
    }

    public static Specification<Student> ofTeacher(User teacher) {
        return (root, query, cb) -> cb.equal(root.get(Student_.createdBy), teacher);
    }

    public Specification<Student> toSpecification() {
        return containsKeyword(keyword);
    }
}
