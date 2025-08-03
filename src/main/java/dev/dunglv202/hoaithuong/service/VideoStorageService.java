package dev.dunglv202.hoaithuong.service;

import com.microsoft.graph.models.DriveItem;
import dev.dunglv202.hoaithuong.dto.LectureVideoMetadataDTO;
import dev.dunglv202.hoaithuong.entity.Lecture;

import java.time.OffsetDateTime;
import java.util.Optional;

/**
 * Retrieve information about where lecture videos are stored
 */
public interface VideoStorageService {
    Optional<DriveItem> findLectureVideo(Lecture lecture, String sourceFolder);

    String createSharableLink(DriveItem item, OffsetDateTime expiration);

    /**
     * @throws RuntimeException Exception while trying to revoke url
     */
    void revokeSharableLink(String itemId);

    String createPreviewLink(String itemId);

    void moveToFolder(DriveItem item, String targetFolder);

    LectureVideoMetadataDTO getMetadata(String videoId);
}
