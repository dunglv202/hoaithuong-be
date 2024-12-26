package dev.dunglv202.hoaithuong.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class User extends Auditable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;

    private String password;

    private String displayName;

    private String avatar;

    private String email;

    private int loginTry;

    private boolean locked;

    @Override
    public String toString() {
        return String.format("[User: %s]", getId());
    }
}
