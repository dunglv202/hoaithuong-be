package dev.dunglv202.hoaithuong.service.impl;

import com.azure.identity.ClientSecretCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.azure.identity.TokenCachePersistenceOptions;
import com.microsoft.graph.drives.item.items.item.createlink.CreateLinkPostRequestBody;
import com.microsoft.graph.models.DriveItem;
import com.microsoft.graph.models.DriveItemCollectionResponse;
import com.microsoft.graph.models.Permission;
import com.microsoft.graph.models.SharingLink;
import com.microsoft.graph.serviceclient.GraphServiceClient;
import dev.dunglv202.hoaithuong.config.prop.MicrosoftProperties;
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
     * @return Lecture video url if existed
     */
    @Override
    public Optional<String> getLectureVideo(Lecture lecture) {
        Optional<DriveItem> containingFolder = getLectureFolder(lecture);
        if (containingFolder.isEmpty()) return Optional.empty();

        Optional<DriveItem> video = getVideoFromFolder(containingFolder.get());
        if (video.isEmpty()) return Optional.empty();

        // create sharable link
        assert video.get().getId() != null;
        CreateLinkPostRequestBody requestBody = new CreateLinkPostRequestBody();
        requestBody.setScope("anonymous");
        requestBody.setType("view");
        requestBody.setExpirationDateTime(OffsetDateTime.now().plusDays(15));
        Permission permission = getGraphClient().drives().byDriveId(microsoftProperties.getDriveId())
            .items().byDriveItemId(video.get().getId())
            .createLink().post(requestBody);

        return Optional.ofNullable(permission).map(Permission::getLink).map(SharingLink::getWebUrl);
    }

    /**
     * @return Video file in folder (first one found)
     */
    private Optional<DriveItem> getVideoFromFolder(DriveItem containingFolder) {
        assert containingFolder.getId() != null;

        DriveItemCollectionResponse itemCollection = getGraphClient().drives().byDriveId(microsoftProperties.getDriveId())
            .items().byDriveItemId(containingFolder.getId())
            .children().get();
        if (itemCollection == null || itemCollection.getValue() == null) return Optional.empty();

        return itemCollection.getValue()
            .stream()
            .filter(this::isVideo)
            .findFirst();
    }

    /**
     * @return The folder that contains actual video based on its name
     */
    private Optional<DriveItem> getLectureFolder(Lecture lecture) {
        GraphServiceClient graphClient = getGraphClient();

        DriveItemCollectionResponse itemCollection = graphClient.drives().byDriveId(microsoftProperties.getDriveId())
            .items().byDriveItemId(microsoftProperties.getVideosFolderId())
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

            return (videoStart.isBefore(schedule.getStartTime()) && diff.toMinutes() <= 5)
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
            .tokenCachePersistenceOptions(new TokenCachePersistenceOptions())
            .build();
        return new GraphServiceClient(credential, scopes);
    }

    private boolean isVideo(DriveItem driveItem) {
        return driveItem.getFile() != null
            && driveItem.getFile().getMimeType() != null
            && driveItem.getFile().getMimeType().startsWith("video");
    }
}
