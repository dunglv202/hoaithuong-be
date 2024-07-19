package dev.dunglv202.hoaithuong.repository;

import dev.dunglv202.hoaithuong.entity.Notification;
import dev.dunglv202.hoaithuong.entity.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    @Query("""
        SELECT COUNT(*)
        FROM Notification n
        WHERE n.user = :user AND n.read = FALSE
        ORDER BY n.timestamp DESC
    """)
    int countAllUnreadByUser(@Param("user") User user);

    @Query("""
        FROM Notification n
        WHERE n.user = :user
        ORDER BY n.timestamp DESC
    """)
    List<Notification> findAllByUser(@Param("user") User user, Pageable pageable);
}
