package dev.dunglv202.hoaithuong.helper;

import org.apache.tika.Tika;
import org.springframework.web.multipart.MultipartFile;

import static dev.dunglv202.hoaithuong.constant.Configuration.SUPPORTED_IMAGE_TYPES;

public class FileUtil {
    public static boolean isSupportedImage(MultipartFile file) {
        return isSupportedImageType(detectFileType(file));
    }

    public static String detectFileType(MultipartFile file) {
        try {
            Tika tika = new Tika();
            return tika.detect(file.getInputStream());
        } catch (Exception e) {
            throw new RuntimeException("Could not detect file type", e);
        }
    }

    private static boolean isSupportedImageType(String mediaType) {
        return SUPPORTED_IMAGE_TYPES.contains(mediaType);
    }
}
