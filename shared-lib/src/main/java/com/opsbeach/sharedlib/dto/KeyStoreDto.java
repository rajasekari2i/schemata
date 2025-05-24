package com.opsbeach.sharedlib.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.security.PrivateKey;
import java.security.interfaces.RSAPublicKey;

/**
 * <p>
 *     Kyestore dto.
 * </p>
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KeyStoreDto {
    private PrivateKey privateKey;
    private RSAPublicKey publicKey;
}
