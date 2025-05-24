package com.opsbeach.sharedlib.security;

import com.opsbeach.sharedlib.exception.ErrorCode;
import com.opsbeach.sharedlib.exception.UnAuthorizedException;
import com.opsbeach.sharedlib.response.ResponseMessage;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Component
public class HMac {

    private static final String H_MAC_SHA256 = "HmacSHA256";

    private final ResponseMessage responseMessage;

    public HMac(ResponseMessage responseMessage) {
        this.responseMessage = responseMessage;
    }

    private byte[] calcHmacSha256(byte[] secretKey, byte[] message) {
        byte[] hmacSha256;
        try {
            var mac = Mac.getInstance(H_MAC_SHA256);
            var secretKeySpec = new SecretKeySpec(secretKey, H_MAC_SHA256);
            mac.init(secretKeySpec);
            hmacSha256 = mac.doFinal(message);
        } catch (Exception e) {
            throw new UnAuthorizedException(ErrorCode.INVALID_ACCESS_TOKEN, responseMessage.getErrorMessage(ErrorCode.INVALID_ACCESS_TOKEN));
        }
        return hmacSha256;
    }

    public String getPayload(String request, String secret) {
        byte[] hmacSha256 = calcHmacSha256(secret.getBytes(StandardCharsets.UTF_8), request.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(hmacSha256);
    }
}