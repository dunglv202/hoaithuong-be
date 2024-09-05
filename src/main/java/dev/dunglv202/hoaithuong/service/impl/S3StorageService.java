package dev.dunglv202.hoaithuong.service.impl;

import dev.dunglv202.hoaithuong.helper.FileUtil;
import dev.dunglv202.hoaithuong.model.AWSProperties;
import dev.dunglv202.hoaithuong.service.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class S3StorageService implements StorageService {
    private final Path tmpFilePath = Paths.get("src/main/resources/");
    private final S3Client s3Client;
    private final AWSProperties awsProperties;

    @Override
    public String storeFile(MultipartFile file) {
        try {
            // generate file name
            String fileName;
            do { fileName = generateFileName(); } while (isExisted(fileName));

            // convert & upload
            Path tempFile = Files.createTempFile("file-", "-tmp");
            file.transferTo(tempFile);
            String fileType = FileUtil.detectFileType(file);
            var putObjReq = PutObjectRequest.builder()
                .bucket(awsProperties.getBucket()).key(fileName)
                .contentType(fileType).contentDisposition(isPreviewable(fileType) ? "inline" : "attachment")
                .build();
            s3Client.putObject(putObjReq, tempFile);

            // get url info then return
            var getObjReq = GetUrlRequest.builder().bucket(awsProperties.getBucket()).key(fileName).build();
            return s3Client.utilities().getUrl(getObjReq).toString();
        } catch (Exception e) {
            throw new RuntimeException("Could not store file", e);
        }
    }

    @Override
    public void deleteFile(String key) {
        var delReq = DeleteObjectRequest.builder().bucket(awsProperties.getBucket()).key(key).build();
        s3Client.deleteObject(delReq);
    }

    private boolean isPreviewable(String fileType) {
        return fileType.startsWith("image") || fileType.startsWith("video")
            || fileType.startsWith("audio") || fileType.startsWith("text");
    }

    private boolean isExisted(String fileName) {
        try {
            s3Client.headObject(HeadObjectRequest.builder().bucket(awsProperties.getBucket()).key(fileName).build());
            return true;
        } catch (NoSuchKeyException e) {
            return false;
        }
    }

    private String generateFileName() {
        return UUID.randomUUID().toString();
    }
}
