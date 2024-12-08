package dev.dunglv202.hoaithuong.repository.impl;

import dev.dunglv202.hoaithuong.entity.Lecture;
import dev.dunglv202.hoaithuong.entity.User;
import dev.dunglv202.hoaithuong.repository.LectureCustomRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;

import java.util.Optional;

public class LectureCustomRepositoryImpl implements LectureCustomRepository {
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Optional<Lecture> findLatestByTeacher(User teacher) {
        TypedQuery<Lecture> query = entityManager.createQuery("""
            SELECT l
            FROM Lecture l
            WHERE l.teacher = :teacher
            ORDER BY l.schedule.startTime DESC
        """, Lecture.class);
        query.setParameter("teacher", teacher);
        query.setMaxResults(1);
        return Optional.ofNullable(query.getSingleResult());
    }
}
