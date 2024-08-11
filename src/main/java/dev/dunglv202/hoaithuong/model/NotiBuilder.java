package dev.dunglv202.hoaithuong.model;

import dev.dunglv202.hoaithuong.constant.NotiType;
import dev.dunglv202.hoaithuong.entity.Notification;
import dev.dunglv202.hoaithuong.entity.User;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class NotiBuilder {
    private final Notification notification = new Notification();

    public static NotiBuilder forUser(User user) {
        NotiBuilder builder = new NotiBuilder();
        builder.notification.setUser(user);
        return builder;
    }

    public NotiBuilder content(String content) {
        notification.setContent(content);
        return this;
    }

    public NotiBuilder type(NotiType type) {
        notification.setType(type);
        return this;
    }

    public NotiBuilder payload(Object payload) {
        notification.setPayload(payload);
        return this;
    }

    public NotiBuilder read() {
        notification.setRead(true);
        return this;
    }

    public Notification build() {
        return notification;
    }
}
