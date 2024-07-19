package dev.dunglv202.hoaithuong.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Getter
@Setter
public class Notification extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String content;

    private boolean read;

    private Instant timestamp;

    @ManyToOne(fetch = FetchType.LAZY)
    private User user;
}
