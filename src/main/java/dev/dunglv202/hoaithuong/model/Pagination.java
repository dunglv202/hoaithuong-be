package dev.dunglv202.hoaithuong.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@Getter
@Setter
public class Pagination {
    private int page = 1;
    private int size = 20;
    @Setter(AccessLevel.PRIVATE)
    private Sort sort;

    public Pagination limit(int limit) {
        this.size = Math.min(size, limit);
        return this;
    }

    public Pagination withSort(Sort sort) {
        this.sort = sort;
        return this;
    }

    public Pageable pageable() {
        if (sort == null) return Pageable.ofSize(size).withPage(page - 1);
        return PageRequest.of(page - 1, size).withSort(sort);
    }
}
