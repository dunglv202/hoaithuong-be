package dev.dunglv202.hoaithuong.repository;

import dev.dunglv202.hoaithuong.entity.Lecture;
import dev.dunglv202.hoaithuong.entity.User;

import java.util.Optional;

public interface LectureCustomRepository {
    Optional<Lecture> findLatestByTeacher(User teacher);
}
