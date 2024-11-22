package dev.dunglv202.hoaithuong.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@ToString
public class Report extends Auditable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int year;

    private int month;

    @ManyToOne(fetch = FetchType.LAZY)
    private User teacher;

    private String confirmation;
}
