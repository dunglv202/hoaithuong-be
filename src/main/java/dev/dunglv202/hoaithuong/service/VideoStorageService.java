package dev.dunglv202.hoaithuong.service;

import com.microsoft.graph.models.DriveItem;
import dev.dunglv202.hoaithuong.entity.Lecture;

import java.util.Optional;

/**
 * Retrieve information about where lecture videos are stored
 */
public interface VideoStorageService {
    Optional<DriveItem> findLectureVideo(Lecture lecture, String sourceFolder);

    String createSharableLink(DriveItem item);

    void moveToFolder(DriveItem item, String targetFolder);
}
