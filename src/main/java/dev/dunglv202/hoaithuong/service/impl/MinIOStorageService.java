package dev.dunglv202.hoaithuong.service.impl;

import dev.dunglv202.hoaithuong.config.prop.MinIOProperties;
import dev.dunglv202.hoaithuong.service.StorageService;
import io.minio.*;
import io.minio.errors.ErrorResponseException;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class MinIOStorageService implements StorageService {
    private final MinIOProperties minIOProperties;
    private final MinioClient minioClient;

    @PostConstruct
    public void postConstruct() {
        log.info("MinIO starting as primary storage");
    }

    @Override
    public String storeFile(MultipartFile file) {
        try {
            String bucket = minIOProperties.getBucket();
            InputStream inputStream = file.getInputStream();
            String objectName;
            do {
                objectName = UUID.randomUUID().toString();
            } while (isObjectExisted(bucket, objectName));

            PutObjectArgs putObjectArgs = PutObjectArgs.builder()
                .bucket(bucket)
                .object(objectName)
                .stream(inputStream, inputStream.available(), -1)
                .contentType(file.getContentType())
                .build();

            ObjectWriteResponse writeResponse = minioClient.putObject(putObjectArgs);
            return minIOProperties.getUrl() + "/" + bucket + "/" + writeResponse.object();
        } catch (Exception e) {
            throw new RuntimeException("Error occurred: " + e.getMessage());
        }
    }

    @Override
    public void deleteFile(String name) {
        try {
            String bucket = minIOProperties.getBucket();
            RemoveObjectArgs removeObjectArgs = RemoveObjectArgs.builder()
                .bucket(bucket)
                .object(name)
                .build();
            minioClient.removeObject(removeObjectArgs);
        } catch (Exception e) {
            log.error("Error occurred while delete file {}: {}", name, e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private boolean isObjectExisted(String bucketName, String objectName) {
        try {
            minioClient.statObject(StatObjectArgs.builder()
                .bucket(bucketName)
                .object(objectName)
                .build());
            return true;
        } catch (ErrorResponseException e) {
            return false;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }
}

