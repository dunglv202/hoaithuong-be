package dev.dunglv202.hoaithuong.model;

import lombok.Getter;

import java.util.List;

@Getter
public class Page<T> {
    private final long totalElements;
    private final List<T> content;

    public Page(org.springframework.data.domain.Page<T> page) {
        this.totalElements = page.getTotalElements();
        this.content = page.getContent();
    }
}
