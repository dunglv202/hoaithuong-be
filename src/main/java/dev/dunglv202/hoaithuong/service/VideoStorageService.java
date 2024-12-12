package dev.dunglv202.hoaithuong.service;

import dev.dunglv202.hoaithuong.entity.Lecture;

import java.util.Optional;

/**
 * Retrieve information about where lecture videos are stored
 */
public interface VideoStorageService {
    Optional<String> getLectureVideo(Lecture lecture);
}
