package dev.dunglv202.hoaithuong.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Monthly report
 */
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

    /**
     * Drive folder id that contains all confirmations for month report
     */
    private String confirmation;
}
