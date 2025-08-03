package dev.dunglv202.hoaithuong.helper;

import lombok.AllArgsConstructor;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import java.security.SecureRandom;
import java.util.Base64;

@AllArgsConstructor
public class IdEncryptor {
    private static final String ENCRYPTION_ALGO = "AES/GCM/NoPadding";
    private static final int GCM_TAG_LENGTH = 96;
    private static final int IV_SIZE = 12;

    private final SecretKey key;

    public String encrypt(String id) throws RuntimeException {
        assert id != null;

        try {
            byte[] iv = new byte[IV_SIZE];
            new SecureRandom().nextBytes(iv);

            Cipher cipher = Cipher.getInstance(ENCRYPTION_ALGO);
            cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(GCM_TAG_LENGTH, iv));

            byte[] encrypted = cipher.doFinal(id.getBytes());
            byte[] encryptedWithIv = new byte[IV_SIZE + encrypted.length];
            System.arraycopy(iv, 0, encryptedWithIv, 0, IV_SIZE);
            System.arraycopy(encrypted, 0, encryptedWithIv, IV_SIZE, encrypted.length);

            return Base64.getUrlEncoder().withoutPadding().encodeToString(encryptedWithIv);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String decrypt(String encryptedStr) throws RuntimeException {
        assert encryptedStr != null;

        try {
            byte[] decoded = Base64.getUrlDecoder().decode(encryptedStr);
            byte[] iv = new byte[IV_SIZE];
            byte[] encrypted = new byte[decoded.length - IV_SIZE];

            System.arraycopy(decoded, 0, iv, 0, IV_SIZE);
            System.arraycopy(decoded, IV_SIZE, encrypted, 0, encrypted.length);

            Cipher cipher = Cipher.getInstance(ENCRYPTION_ALGO);
            cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(GCM_TAG_LENGTH, iv));

            byte[] decrypted = cipher.doFinal(encrypted);
            return new String(decrypted);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // Example key generator
    public static SecretKey generateKey() throws Exception {
        KeyGenerator generator = KeyGenerator.getInstance("AES");
        generator.init(256); // Use 128 if 256 not available
        return generator.generateKey();
    }
}
