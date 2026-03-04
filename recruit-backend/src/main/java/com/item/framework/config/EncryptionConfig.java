package com.item.framework.config;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;

/**
 * Immutable configuration object for encryption scenarios.
 * Thread-safe value object that stores encryption parameters.
 *
 * @author hua.liu
 */
public final class EncryptionConfig {

    private final String name;
    private final long prime;
    private final int checksumLength;
    private final byte[] hmacKeyBytes;
    private final String hmacAlgorithm;

    /**
     * Constructs an EncryptionConfig with validation.
     *
     * @param name           Scenario name (must not be blank)
     * @param prime          Prime number for XOR obfuscation (must be > 0)
     * @param checksumLength Length of checksum to extract (must be 1-64)
     * @param hmacKeyBytes   HMAC key bytes (must be exactly 32 bytes)
     * @param hmacAlgorithm  HMAC algorithm name (defaults to "HmacSHA256" if null)
     * @throws IllegalArgumentException if any parameter is invalid
     */
    public EncryptionConfig(String name, long prime, int checksumLength, 
                           byte[] hmacKeyBytes, String hmacAlgorithm) {
        // Validate name
        if (StringUtils.isBlank(name)) {
            throw new IllegalArgumentException("Scenario name must not be blank");
        }

        // Validate prime
        if (prime <= 0) {
            throw new IllegalArgumentException(
                String.format("Prime must be positive, got: %d", prime));
        }

        // Validate checksumLength
        if (checksumLength < 1 || checksumLength > 64) {
            throw new IllegalArgumentException(
                String.format("Checksum length must be between 1 and 64, got: %d", 
                    checksumLength));
        }

        // Validate hmacKeyBytes
        if (hmacKeyBytes == null || hmacKeyBytes.length != 32) {
            throw new IllegalArgumentException(
                String.format("HMAC key must be exactly 32 bytes (256 bits), got: %d bytes", 
                    hmacKeyBytes == null ? 0 : hmacKeyBytes.length));
        }

        // Set fields (defensive copy for byte array)
        this.name = name;
        this.prime = prime;
        this.checksumLength = checksumLength;
        this.hmacKeyBytes = Arrays.copyOf(hmacKeyBytes, hmacKeyBytes.length);
        this.hmacAlgorithm = StringUtils.isBlank(hmacAlgorithm) ? "HmacSHA256" : hmacAlgorithm;
    }

    /**
     * Convenience constructor with default HMAC algorithm.
     */
    public EncryptionConfig(String name, long prime, int checksumLength, byte[] hmacKeyBytes) {
        this(name, prime, checksumLength, hmacKeyBytes, "HmacSHA256");
    }

    public String getName() {
        return name;
    }

    public long getPrime() {
        return prime;
    }

    public int getChecksumLength() {
        return checksumLength;
    }

    /**
     * Returns a defensive copy of the HMAC key bytes.
     */
    public byte[] getHmacKeyBytes() {
        return Arrays.copyOf(hmacKeyBytes, hmacKeyBytes.length);
    }

    public String getHmacAlgorithm() {
        return hmacAlgorithm;
    }

    @Override
    public String toString() {
        return String.format("EncryptionConfig{name='%s', prime=%d, checksumLength=%d, hmacAlgorithm='%s'}",
            name, prime, checksumLength, hmacAlgorithm);
    }
}
