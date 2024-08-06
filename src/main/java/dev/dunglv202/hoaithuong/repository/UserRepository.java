package dev.dunglv202.hoaithuong.repository;

import dev.dunglv202.hoaithuong.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    @Query("""
        FROM User u
        WHERE (:username IS NOT NULL) AND (u.username = :username OR u.email = :username)
    """)
    Optional<User> findByUsernameOrEmail(@Param("username") String username);

    @Query("""
        FROM User u
        WHERE u.email = :email
    """)
    Optional<User> findByEmail(@NonNull @Param("email") String email);
}
