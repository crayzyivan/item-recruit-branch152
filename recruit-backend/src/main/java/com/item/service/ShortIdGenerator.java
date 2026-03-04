package com.item.service;

/**
 * @author : lh
 */
public interface ShortIdGenerator {
    String generateShortId(Long id);

    Long parseShortId(String shortId);

    boolean isValidShortId(String shortId);

    /**
     * Encrypt candidate ID for sharing
     * @param candidateId Candidate ID to encrypt
     * @return Encrypted candidate ID string
     */
    String encryptCandidateId(Long candidateId);

    /**
     * Decrypt encrypted candidate ID
     * @param encryptedId Encrypted candidate ID string
     * @return Original candidate ID, or null if decryption fails
     */
    Long decryptCandidateId(String encryptedId);

    /**
     * Encrypt an ID using a specific encryption scenario.
     *
     * @param id           The ID to encrypt (must be positive)
     * @param scenarioName The name of the encryption scenario to use
     * @return The encrypted ID string
     * @throws IllegalArgumentException if id is null/non-positive or scenarioName doesn't exist
     */
    String encrypt(Long id, String scenarioName);

    /**
     * Decrypt an encrypted ID using a specific encryption scenario.
     *
     * @param encryptedId  The encrypted ID string
     * @param scenarioName The name of the encryption scenario to use
     * @return The original ID, or null if decryption fails
     * @throws IllegalArgumentException if scenarioName doesn't exist
     */
    Long decrypt(String encryptedId, String scenarioName);
}
