package dev.dunglv202.hoaithuong.model;

public interface Range<T> {
    T getFrom();
    T getTo();

    static <T> Range<T> from(T from) {
        return SimpleRange.from(from);
    }
}
