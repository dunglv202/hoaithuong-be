package dev.dunglv202.hoaithuong.service.impl;

import com.google.api.client.http.InputStreamContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.Permission;
import dev.dunglv202.hoaithuong.entity.User;
import dev.dunglv202.hoaithuong.helper.FileUtil;
import dev.dunglv202.hoaithuong.helper.GoogleHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class GoogleDriveService {
    private final GoogleHelper googleHelper;

    /**
     * @return Created folder ID
     */
    public String createDriveFolder(User user, String name) {
        try {
            Drive driveService = googleHelper.getDriveService(user);

            File folder = new File();
            folder.setName(name);
            folder.setMimeType("application/vnd.google-apps.folder");
            Permission permission = new Permission();
            permission.setType("anyone");
            permission.setRole("reader");

            File createdFolder = driveService.files().create(folder).setFields("id").execute();
            driveService.permissions().create(createdFolder.getId(), permission).execute();

            return createdFolder.getId();
        } catch (IOException e) {
            log.error("Could not create drive folder for {}", user.getUsername(), e);
            throw new RuntimeException(e);
        }
    }

    /**
     * @return Created file ID
     */
    public String uploadToFolder(User user, String folderId, MultipartFile multipartFile, String name) {
        try {
            Drive driveService = googleHelper.getDriveService(user);
            File metadata = new File();
            metadata.setName(name);
            metadata.setParents(List.of(folderId));
            String mediaType = FileUtil.detectFileType(multipartFile);
            InputStreamContent content = new InputStreamContent(mediaType, multipartFile.getInputStream());
            File createdFile = driveService.files().create(metadata, content).setFields("id").execute();
            return createdFile.getId();
        } catch (IOException e) {
            log.error("Could not upload to drive for {}", user.getUsername(), e);
            throw new RuntimeException(e);
        }
    }

    public void deleteFile(User user, String fileId) {
        try {
            Drive driveService = googleHelper.getDriveService(user);
            driveService.files().delete(fileId).execute();
        } catch (IOException e) {
            log.error("Could not delete drive file for {}", user.getUsername(), e);
            throw new RuntimeException(e);
        }
    }

    public File makeCopy(User user, String fileId, String name) {
        try {
            Drive driveService = googleHelper.getDriveService(user);
            return driveService.files().copy(fileId, new File().setName(name)).execute();
        } catch (IOException e) {
            log.error("Could not make copy for {} - {}", fileId, user.getUsername(), e);
            throw new RuntimeException(e);
        }
    }

    public void moveToFolder(User user, File file, String folderId) {
        try {
            Drive driveService = googleHelper.getDriveService(user);

            // Retrieve the existing parents to remove
            File updatingFile = driveService.files().get(file.getId())
                .setFields("parents")
                .execute();
            StringBuilder previousParents = new StringBuilder();
            for (String parent : updatingFile.getParents()) {
                previousParents.append(parent);
                previousParents.append(',');
            }

            // Move the file to the new folder
            driveService.files().update(file.getId(), null)
                .setAddParents(folderId)
                .setRemoveParents(previousParents.toString())
                .execute();
        } catch (IOException e) {
            log.error("Unable to move file {} to {} for {}", file.getId(), folderId, user.getEmail(), e);
            throw new RuntimeException(e);
        }
    }
}
