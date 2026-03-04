package com.item.service.encryption;

import com.item.framework.config.EncryptionConfig;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread-safe registry for managing encryption configurations.
 * Uses ConcurrentHashMap for lock-free concurrent access.
 *
 * @author hua.liu
 */
public class EncryptionConfigRegistry {

    private final ConcurrentHashMap<String, EncryptionConfig> configs;

    public EncryptionConfigRegistry() {
        this.configs = new ConcurrentHashMap<>();
    }

    /**
     * Registers an encryption configuration.
     *
     * @param config The configuration to register
     * @throws IllegalStateException if a configuration with the same name already exists
     */
    public void register(EncryptionConfig config) {
        if (config == null) {
            throw new IllegalArgumentException("Configuration cannot be null");
        }

        String scenarioName = config.getName();
        EncryptionConfig existing = configs.putIfAbsent(scenarioName, config);

        if (existing != null) {
            throw new IllegalStateException(
                String.format("Duplicate scenario name '%s'. Scenario names must be unique.", 
                    scenarioName));
        }
    }

    /**
     * Retrieves an encryption configuration by scenario name.
     *
     * @param scenarioName The name of the scenario
     * @return The encryption configuration, or null if not found
     */
    public EncryptionConfig get(String scenarioName) {
        return configs.get(scenarioName);
    }

    /**
     * Checks if a scenario exists in the registry.
     *
     * @param scenarioName The name of the scenario
     * @return true if the scenario exists, false otherwise
     */
    public boolean contains(String scenarioName) {
        return configs.containsKey(scenarioName);
    }

    /**
     * Returns all registered scenario names.
     *
     * @return Set of scenario names
     */
    public Set<String> getScenarioNames() {
        return configs.keySet();
    }

    /**
     * Returns the number of registered scenarios.
     *
     * @return Number of scenarios
     */
    public int size() {
        return configs.size();
    }

    /**
     * Checks if the registry is empty.
     *
     * @return true if no scenarios are registered
     */
    public boolean isEmpty() {
        return configs.isEmpty();
    }
}
