package dev.dunglv202.hoaithuong.service.impl;

import com.azure.identity.ClientSecretCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.microsoft.graph.drives.item.items.item.createlink.CreateLinkPostRequestBody;
import com.microsoft.graph.drives.item.items.item.preview.PreviewPostRequestBody;
import com.microsoft.graph.models.*;
import com.microsoft.graph.serviceclient.GraphServiceClient;
import dev.dunglv202.hoaithuong.config.prop.MicrosoftProperties;
import dev.dunglv202.hoaithuong.dto.LectureVideoMetadataDTO;
import dev.dunglv202.hoaithuong.entity.Lecture;
import dev.dunglv202.hoaithuong.entity.Schedule;
import dev.dunglv202.hoaithuong.service.VideoStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class OneDriveService implements VideoStorageService {
    private static final Pattern DATE_TIME_PATTERN = Pattern.compile(
        "^(\\d{4}-\\d{2}-\\d{2} \\d{2}\\.\\d{2}\\.\\d{2}).*"
    );

    private final MicrosoftProperties microsoftProperties;

    /**
     * @param sourceFolder Folder ID where to find video
     * @return Lecture video url if existed
     */
    @Override
    public Optional<DriveItem> findLectureVideo(Lecture lecture, String sourceFolder) {
        Optional<DriveItem> containingFolder = findLectureFolder(lecture, sourceFolder);
        if (containingFolder.isEmpty()) return Optional.empty();
        return findVideoInFolder(containingFolder.get());
    }

    @Override
    public String createSharableLink(DriveItem item, OffsetDateTime expiration) {
        assert item.getId() != null;

        CreateLinkPostRequestBody requestBody = new CreateLinkPostRequestBody();
        requestBody.setScope("anonymous");
        requestBody.setType("view");
        requestBody.setExpirationDateTime(expiration);
        Permission permission = getGraphClient().drives().byDriveId(microsoftProperties.getDriveId())
            .items().byDriveItemId(item.getId())
            .createLink().post(requestBody);

        return Optional.ofNullable(permission).map(Permission::getLink).map(SharingLink::getWebUrl)
            .orElseThrow(() -> new RuntimeException("Could not create sharable link for item: " + item.getId()));
    }

    @Override
    public void revokeSharableLink(String itemId) {
        PermissionCollectionResponse permissionCollection = getGraphClient().drives().byDriveId(microsoftProperties.getDriveId())
            .items().byDriveItemId(itemId)
            .permissions()
            .get();

        if (permissionCollection == null || permissionCollection.getValue() == null) {
            throw new RuntimeException("Could not retrieve permissions for item #" + itemId);
        }

        Predicate<Permission> sharedToAnonymous = p -> "anonymous".equals(Optional.ofNullable(p.getLink()).map(SharingLink::getScope).orElse(null));
        Optional<Permission> anonymousSharedPermission = permissionCollection.getValue().stream()
            .filter(sharedToAnonymous)
            .findFirst();

        if (anonymousSharedPermission.isEmpty() || anonymousSharedPermission.get().getId() == null) {
            throw new RuntimeException("Item has not been shared yet: #" + itemId);
        }

        // revoke permission
        getGraphClient().drives().byDriveId(microsoftProperties.getDriveId())
            .items().byDriveItemId(itemId)
            .permissions().byPermissionId(anonymousSharedPermission.get().getId())
            .delete();
    }

    @Override
    public String createPreviewLink(String itemId) {
        assert itemId != null;

        ItemPreviewInfo previewInfo = getGraphClient().drives().byDriveId(microsoftProperties.getDriveId())
            .items().byDriveItemId(itemId)
            .preview().post(new PreviewPostRequestBody());

        return Optional.ofNullable(previewInfo)
            .map(ItemPreviewInfo::getGetUrl)
            .orElseThrow(() -> new RuntimeException("Could not create preview url for item: " + itemId));
    }

    @Override
    public void moveToFolder(DriveItem item, String targetFolder) {
        assert item.getId() != null;

        ItemReference parentRef = new ItemReference();
        parentRef.setId(targetFolder);
        DriveItem modified = new DriveItem();
        modified.setParentReference(parentRef);

        getGraphClient().drives().byDriveId(microsoftProperties.getDriveId())
            .items().byDriveItemId(item.getId())
            .patch(modified);
    }

    @Override
    public LectureVideoMetadataDTO getMetadata(String videoId) {
        DriveItem driveItem = getGraphClient().drives().byDriveId(microsoftProperties.getDriveId())
            .items().byDriveItemId(videoId)
            .get(conf -> {
                assert conf.queryParameters != null;
                conf.queryParameters.expand = new String[]{ "thumbnails" };
            });
        if (driveItem == null) return LectureVideoMetadataDTO.empty();

        String title = driveItem.getName();
        String description = driveItem.getDescription();
        String largeThumb = Optional.ofNullable(driveItem.getThumbnails())
            .map(thumbs -> thumbs.isEmpty() ? null : thumbs.get(0))
            .map(ThumbnailSet::getLarge)
            .map(Thumbnail::getUrl).orElse(null);

        return LectureVideoMetadataDTO.builder()
            .title(title)
            .description(description)
            .thumbnailUrl(largeThumb)
            .build();
    }

    /**
     * @return Video file in folder (largest file)
     */
    private Optional<DriveItem> findVideoInFolder(DriveItem containingFolder) {
        assert containingFolder.getId() != null;

        DriveItemCollectionResponse itemCollection = getGraphClient().drives().byDriveId(microsoftProperties.getDriveId())
            .items().byDriveItemId(containingFolder.getId())
            .children().get();
        if (itemCollection == null || itemCollection.getValue() == null) return Optional.empty();

        return itemCollection.getValue()
            .stream()
            .filter(this::isVideo)
            .max(this::compareByFileSize);
    }

    private int compareByFileSize(DriveItem a, DriveItem b) {
        if (b.getSize() == null) return 1;
        if (a.getSize() == null) return b.getSize() != null ? -1 : 1;
        return a.getSize().compareTo(b.getSize());
    }

    /**
     * @return The folder that contains actual video based on its name
     */
    private Optional<DriveItem> findLectureFolder(Lecture lecture, String sourceFolder) {
        GraphServiceClient graphClient = getGraphClient();

        DriveItemCollectionResponse itemCollection = graphClient.drives().byDriveId(microsoftProperties.getDriveId())
            .items().byDriveItemId(sourceFolder)
            .children().get();
        if (itemCollection == null || itemCollection.getValue() == null) return Optional.empty();

        return itemCollection.getValue()
            .stream()
            .filter(item -> matchScheduleByName(item, lecture.getSchedule()))
            .findFirst();
    }

    private boolean matchScheduleByName(DriveItem driveItem, Schedule schedule) {
        if (driveItem.getName() == null) return false;

        // file format: YYYY-MM-DD HH.mm.ss
        Matcher matcher = DATE_TIME_PATTERN.matcher(driveItem.getName());
        if (!matcher.matches()) return false;

        try {
            LocalDateTime videoStart = LocalDateTime.parse(
                matcher.group(1),
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH.mm.ss")
            );
            Duration diff = Duration.between(videoStart, schedule.getStartTime()).abs();

            return (videoStart.isBefore(schedule.getStartTime()) && diff.toMinutes() <= 10)
                || (videoStart.isAfter(schedule.getStartTime()) && diff.toMinutes() <= 30);
        } catch (Exception e) {
            log.error("Could not parse date time from '{}'", driveItem.getName(), e);
            return false;
        }
    }

    private GraphServiceClient getGraphClient() {
        String[] scopes = new String[]{ "https://graph.microsoft.com/.default" };
        ClientSecretCredential credential = new ClientSecretCredentialBuilder()
            .clientId(microsoftProperties.getClientId())
            .tenantId(microsoftProperties.getTenantId())
            .clientSecret(microsoftProperties.getClientSecret())
            .build();
        return new GraphServiceClient(credential, scopes);
    }

    private boolean isVideo(DriveItem driveItem) {
        return driveItem.getFile() != null
            && driveItem.getFile().getMimeType() != null
            && driveItem.getFile().getMimeType().startsWith("video");
    }
}
