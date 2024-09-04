package dev.dunglv202.hoaithuong.entity;

import dev.dunglv202.hoaithuong.dto.UpdatedStudentDTO;
import dev.dunglv202.hoaithuong.mapper.StudentMapper;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Formula;

@Entity
@Getter
@Setter
public class Student extends Auditable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Formula("(SELECT COUNT(*) > 0 FROM tutor_class c WHERE c.student_id = id AND c.active = TRUE)")
    private boolean active;

    private String notes;

    @Embedded
    @AttributeOverride(name = "salutation", column = @Column(name = "report_to_salutation"))
    @AttributeOverride(name = "name", column = @Column(name = "report_to_name"))
    private Person reportTo;

    public Student merge(UpdatedStudentDTO updated) {
        StudentMapper.INSTANCE.mergeStudent(this, updated);
        return this;
    }
}
