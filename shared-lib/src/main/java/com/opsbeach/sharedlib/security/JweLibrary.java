package com.opsbeach.sharedlib.security;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.nimbusds.jose.EncryptionMethod;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWEAlgorithm;
import com.nimbusds.jose.JWEHeader;
import com.nimbusds.jose.JWEObject;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.RSADecrypter;
import com.nimbusds.jose.crypto.RSAEncrypter;
import com.opsbeach.sharedlib.dto.JweDto;
import com.opsbeach.sharedlib.dto.KeyStoreDto;
import org.springframework.stereotype.Component;

import java.security.interfaces.RSAPublicKey;
import java.text.ParseException;

@Component
public class JweLibrary {
    private static final JWEAlgorithm ALGORITHM = JWEAlgorithm.RSA_OAEP_256;
    private static final EncryptionMethod ENCRYPTION = EncryptionMethod.A128CBC_HS256;

    public String encrypt(JweDto jweDto, RSAPublicKey publicKey) throws JOSEException, JsonProcessingException {
        var jweJsonObject = JweDto.asJsonObject(jweDto);
        var payload = new Payload(jweJsonObject);
        var jwe = new JWEObject(new JWEHeader(ALGORITHM, ENCRYPTION), payload);
        jwe.encrypt(new RSAEncrypter(publicKey));
        return jwe.serialize();
    }

    public JweDto decrypt(String token, KeyStoreDto keyStoreDto) throws JOSEException, ParseException, JsonProcessingException {
        var jweObject = JWEObject.parse(token);
        jweObject.decrypt(new RSADecrypter(keyStoreDto.getPrivateKey()));
        var payload = jweObject.getPayload();
        return JweDto.fromJsonObject(payload.toJSONObject());
    }
}