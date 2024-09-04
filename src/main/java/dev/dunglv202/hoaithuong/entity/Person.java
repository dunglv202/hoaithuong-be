package dev.dunglv202.hoaithuong.entity;

import dev.dunglv202.hoaithuong.constant.Salutation;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Getter;
import lombok.Setter;

@Embeddable
@Getter
@Setter
public class Person {
    @Enumerated(EnumType.STRING)
    private Salutation salutation;

    private String name;
}
