package dev.dunglv202.hoaithuong.service.impl;

import com.azure.identity.ClientSecretCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.microsoft.graph.models.DriveItem;
import com.microsoft.graph.models.DriveItemCollectionResponse;
import com.microsoft.graph.serviceclient.GraphServiceClient;
import dev.dunglv202.hoaithuong.config.prop.MicrosoftProperties;
import dev.dunglv202.hoaithuong.entity.Lecture;
import dev.dunglv202.hoaithuong.entity.Schedule;
import dev.dunglv202.hoaithuong.service.DriveService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class OneDriveService implements DriveService {
    private static final Pattern DATE_TIME_PATTERN = Pattern.compile(
        "\\b\\d{4}-\\d{2}-\\d{2} \\d{2}\\.\\d{2}\\.\\d{2}\\b"
    );

    private final MicrosoftProperties microsoftProperties;

    /**
     * Return the folder that contains actual video based on its name
     */
    public Optional<DriveItem> getLectureVideo(Lecture lecture) {
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
                matcher.group(),
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
            .build();
        return new GraphServiceClient(credential, scopes);
    }
}
