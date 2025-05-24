package com.opsbeach.sharedlib.security;

import com.nimbusds.jose.EncryptionMethod;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWEAlgorithm;
import com.nimbusds.jose.JWEHeader;
import com.nimbusds.jose.JWEObject;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.RSADecrypter;
import com.nimbusds.jose.crypto.RSAEncrypter;
import com.opsbeach.sharedlib.utils.Constants;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.util.FileCopyUtils;

import java.io.IOException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.text.ParseException;
import java.util.Base64;

/**
 * <p>
 * RSA Encryption Algorithm.
 * </p>
 */
@Component
public class RSAMechanism {
    public static final String RSA = "RSA";
    private static final String PUBLIC_KEY_FILE = "public.pem";
    private static final String PRIVATE_KEY_FILE = "private_pkcs8.pem";
    private static final JWEAlgorithm ALGORITHM = JWEAlgorithm.RSA_OAEP_256;
    private static final EncryptionMethod ENCRYPTION = EncryptionMethod.A128CBC_HS256;
    private final PrivateKey privateKey;
    private final RSAPublicKey publicKey;

    public RSAMechanism() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] privateKeyBytes = FileCopyUtils.copyToByteArray(new ClassPathResource(PRIVATE_KEY_FILE).getInputStream());
        String privateKeyContent = new String(privateKeyBytes).replaceAll("\\n", Constants.EMPTY).replaceAll("\\r", Constants.EMPTY).replace("-----BEGIN PRIVATE KEY-----", Constants.EMPTY).replace("-----END PRIVATE KEY-----", Constants.EMPTY);
        byte[] publicKeyBytes = FileCopyUtils.copyToByteArray(new ClassPathResource(PUBLIC_KEY_FILE).getInputStream());
        String publicKeyContent = new String(publicKeyBytes).replaceAll("\\n", Constants.EMPTY).replaceAll("\\r", Constants.EMPTY).replace("-----BEGIN PUBLIC KEY-----", Constants.EMPTY).replace("-----END PUBLIC KEY-----", Constants.EMPTY);
        var keySpecPKCS8 = new PKCS8EncodedKeySpec(Base64.getDecoder().decode(privateKeyContent));
        var kf = KeyFactory.getInstance(RSA);
        privateKey = kf.generatePrivate(keySpecPKCS8);
        var keySpecX509 = new X509EncodedKeySpec(Base64.getDecoder().decode(publicKeyContent));
        publicKey = (RSAPublicKey) kf.generatePublic(keySpecX509);
    }

    public String encrypt(String encryptString) throws JOSEException {
        var payload = new Payload(encryptString);
        var jwe = new JWEObject(new JWEHeader(ALGORITHM, ENCRYPTION), payload);
        jwe.encrypt(new RSAEncrypter(publicKey));
        return jwe.serialize();
    }

    public String decrypt(String encryptedString) throws JOSEException, ParseException {
        var jweObject = JWEObject.parse(encryptedString);
        jweObject.decrypt(new RSADecrypter(privateKey));
        return jweObject.getPayload().toString();
    }
}
