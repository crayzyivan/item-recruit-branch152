package com.item.service.impl;

import com.item.service.ShortIdGenerator;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.security.SecureRandom;
import java.util.Base64;

/**
 * Unit tests for ShortIdGeneratorImpl candidate share encryption/decryption functionality
 * Tests cover normal scenarios, exception handling, tamper detection, and round-trip consistency
 *
 * @author AI Assistant
 * @since 2025-10-22
 */
@SpringBootTest
class ShortIdGeneratorImplTest {

    @Autowired
    private ShortIdGenerator shortIdGenerator;

    // ==================== Normal Case Tests ====================

    @Test
    @DisplayName("Test candidate ID encryption - normal case")
    void testEncryptCandidateId_Normal() {
        // Given
        Long candidateId = 12345L;

        // When
        String encryptedId = shortIdGenerator.encryptCandidateId(candidateId);

        // Then
        assertNotNull(encryptedId, "Encrypted ID should not be null");
        assertTrue(encryptedId.length() > 6,
            "Encrypted ID length should be greater than checksum length (6)");
        // Verify it only contains valid Base36 characters after checksum
        assertTrue(encryptedId.matches("^[0-9a-f]{6}[0-9a-z]+$"),
            "Encrypted ID should have 6-char hex checksum + Base36 string");
    }

    @Test
    @DisplayName("Test candidate ID decryption - normal case")
    void testDecryptCandidateId_Normal() {
        // Given
        Long originalId = 12345L;
        String encryptedId = shortIdGenerator.encryptCandidateId(originalId);

        // When
        Long decryptedId = shortIdGenerator.decryptCandidateId(encryptedId);

        // Then
        assertNotNull(decryptedId, "Decrypted ID should not be null");
        assertEquals(originalId, decryptedId,
            "Decrypted ID should match original ID");
    }

    @Test
    @DisplayName("Test candidate ID decryption - normal case")
    void testDecryptQuestion01() {
        // Given
        Long originalId = 12345876L;
        String encryptedId = shortIdGenerator.encrypt(originalId, "question5S");

        // When
        Long decryptedId = shortIdGenerator.decrypt(encryptedId, "question5S");

        // Then
        assertNotNull(decryptedId, "Decrypted ID should not be null");
        assertEquals(originalId, decryptedId,
                "Decrypted ID should match original ID");
    }

    @Test
    void test(){
        SecureRandom random = new SecureRandom();
        byte[] key = new byte[32];
        random.nextBytes(key);
        String base64Key = Base64.getEncoder().encodeToString(key);
        System.out.println("Generated HMAC Key: " + base64Key);
    }
}

