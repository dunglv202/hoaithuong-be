package dev.dunglv202.hoaithuong.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

import static lombok.AccessLevel.PROTECTED;

@Entity
@Getter
@NoArgsConstructor(access = PROTECTED)
public class Notification extends Auditable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String content;

    private boolean read = false;

    private Instant timestamp = Instant.now();

    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    public static Notification forUser(User user) {
        Notification notification = new Notification();
        notification.user = user;
        return notification;
    }

    public Notification content(String content) {
        this.content = content;
        return this;
    }

    public Notification read() {
        this.read = true;
        return this;
    }
}
