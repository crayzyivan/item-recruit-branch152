package com.item.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for Md5SignatureUtil
 *
 * @author hua.liu
 */
class Md5SignatureUtilTest {

    private static final String SECRET_KEY = "mySecretKey123";

    @Test
    @DisplayName("Test basic MD5 hash generation")
    void testMd5() {
        // Given
        String input = "Hello World";

        // When
        String hash = Md5SignatureUtil.md5(input);

        // Then
        assertNotNull(hash);
        assertEquals(32, hash.length()); // MD5 produces 32 hex characters
        assertEquals("b10a8db164e0754105b7a99be72e3fe5", hash);
    }

    @Test
    @DisplayName("Test MD5 hash with null input")
    void testMd5WithNull() {
        // When
        String hash = Md5SignatureUtil.md5(null);

        // Then
        assertNull(hash);
    }

    @Test
    @DisplayName("Test MD5 hash with empty string")
    void testMd5WithEmptyString() {
        // When
        String hash = Md5SignatureUtil.md5("");

        // Then
        assertNull(hash);
    }

}
