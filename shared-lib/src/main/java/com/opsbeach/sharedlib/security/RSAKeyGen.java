package com.opsbeach.sharedlib.security;

import com.opsbeach.sharedlib.dto.KeyStoreDto;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.security.KeyFactory;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * <p>
 *    Generate new RSAKey Gen
 * </p>
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RSAKeyGen {

    private static final String RSA = "RSA";

    /**
     * Generates Public and Private key.
     *
     * @return KeyStoreDto    - Dto with Public and Private key.
     */
    public static KeyStoreDto getPublicAndPrivateKey() {
        try {
            var keyPairGenerator = KeyPairGenerator.getInstance(RSA);
            keyPairGenerator.initialize(2048);
            var keyPair = keyPairGenerator.generateKeyPair();
            var publicKeyContent = keyPair.getPublic();
            var privateKeyContent = keyPair.getPrivate();
            var keySpecPKCS8 = new PKCS8EncodedKeySpec(privateKeyContent.getEncoded());
            var keyFactory = KeyFactory.getInstance(RSA);
            var privateKey = keyFactory.generatePrivate(keySpecPKCS8);
            var keySpecX509 = new X509EncodedKeySpec(publicKeyContent.getEncoded());
            var publicKey = (RSAPublicKey) keyFactory.generatePublic(keySpecX509);
            return KeyStoreDto.builder().privateKey(privateKey).publicKey(publicKey).build();
        } catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
            log.error("There is an generating Private and Public Key - {}", e.getMessage());
        }
        return KeyStoreDto.builder().build();
    }

    /**
     * Returns the Public and Private key from the String content.
     *
     * @param privateKeyContent - Private Key content.
     * @param publicKeyContent  - Public Key content.
     * @return KeyStoreDto    - Dto with Public and Private key.
     */
    public static KeyStoreDto getStringToKeys(String publicKeyContent, String privateKeyContent) {
        try {
            var keySpecPKCS8 = new PKCS8EncodedKeySpec(Base64.getDecoder().decode(privateKeyContent));
            var keyFactory = KeyFactory.getInstance(RSA);
            var privateKey = keyFactory.generatePrivate(keySpecPKCS8);
            var keySpecX509 = new X509EncodedKeySpec(Base64.getDecoder().decode(publicKeyContent));
            var publicKey = (RSAPublicKey) keyFactory.generatePublic(keySpecX509);
            return KeyStoreDto.builder().privateKey(privateKey).publicKey(publicKey).build();
        } catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
            log.error("There is an generating Private and Public Key - {}", e.getMessage());
        }
        return KeyStoreDto.builder().build();
    }
}
