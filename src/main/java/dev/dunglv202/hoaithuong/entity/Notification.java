package dev.dunglv202.hoaithuong.entity;

import dev.dunglv202.hoaithuong.constant.NotiType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;

@Entity
@Getter
@Setter
public class Notification extends Auditable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String content;

    private boolean read = false;

    @Enumerated(EnumType.STRING)
    private NotiType type;

    @JdbcTypeCode(SqlTypes.JSON)
    private Object payload;

    /**
     * Processed by user (for notifications that require user's action). Example: REVIEW_CLASS...
     */
    private boolean resolved;

    private Instant timestamp = Instant.now();

    @ManyToOne(fetch = FetchType.LAZY)
    private User user;
}
