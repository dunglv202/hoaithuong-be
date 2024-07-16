package dev.dunglv202.hoaithuong.entity;

import dev.dunglv202.hoaithuong.helper.AuthHelper;
import jakarta.persistence.*;
import lombok.Getter;

import java.time.Instant;

@MappedSuperclass
@Getter
public class BaseEntity {
    private Instant createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    private Instant updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by")
    private User updatedBy;

    @PrePersist
    void prePersist() {
        createdAt = Instant.now();
        createdBy = AuthHelper.getCurrentUser();
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = Instant.now();
        updatedBy = AuthHelper.getCurrentUser();
    }
}
