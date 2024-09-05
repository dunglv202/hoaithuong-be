package dev.dunglv202.hoaithuong.service.impl;

import dev.dunglv202.hoaithuong.helper.FileUtil;
import dev.dunglv202.hoaithuong.model.AWSProperties;
import dev.dunglv202.hoaithuong.service.StorageService;
import lombok.RequiredArgsConstructor;
import org.apache.tika.metadata.Metadata;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetUrlRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class S3StorageService implements StorageService {
    private final Path tmpFilePath = Paths.get("src/main/resources/");
    private final S3Client s3Client;
    private final AWSProperties awsProperties;

    @Override
    public String storeFile(MultipartFile avatar) {
        try {
            // generate file name
            String fileName;
            do { fileName = generateFileName(); } while (isExisted(fileName));

            // convert & upload
            Path tempFile = Files.createTempFile("image-", "-tmp");
            avatar.transferTo(tempFile);
            String mediaType = FileUtil.detectFileType(avatar);
            var putObjReq = PutObjectRequest.builder().bucket(awsProperties.getBucket()).key(fileName)
                .metadata(Map.of(Metadata.CONTENT_TYPE, mediaType)).build();
            s3Client.putObject(putObjReq, tempFile);
            Files.delete(tempFile);

            // get url info then return
            var getObjReq = GetUrlRequest.builder().bucket(awsProperties.getBucket()).key(fileName).build();
            return s3Client.utilities().getUrl(getObjReq).toString();
        } catch (Exception e) {
            throw new RuntimeException("Could not store file", e);
        }
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
