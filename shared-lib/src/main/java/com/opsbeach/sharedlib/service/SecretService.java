package com.opsbeach.sharedlib.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Base64;

@Service
@RequiredArgsConstructor
public class SecretService {

    public String decodeMySecret(String encodedString) {
        return new String(Base64.getDecoder().decode(encodedString));
    }

    public String encodeMySecret(String stringToEncode) {
        return new String(Base64.getEncoder().encode(stringToEncode.getBytes()));
    }
}
