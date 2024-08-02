package dev.dunglv202.hoaithuong.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SimpleRange<T> implements Range<T> {
    private T from;
    private T to;

    public static <T> SimpleRange<T> from(T from) {
        SimpleRange<T> range = new SimpleRange<>();
        range.from = from;
        return range;
    }

    public SimpleRange<T> to(T to) {
        this.to = to;
        return this;
    }
}
