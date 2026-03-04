package com.item.framework.constant;

import lombok.Getter;

/**
 * XML Feed related constants
 *
 * @author liyunlong
 * @since 2025-08-26
 */
public class XmlFeedConstants {

    /**
     * Platform type enum
     */
    @Getter
    public enum PlatformType {
        /** LinkedIn Platform */
        LINKEDIN(1, "LinkedIn"),
        /** Indeed Platform */
        INDEED(2, "Indeed"),
        /** ZipRecruiter Platform */
        ZIP_RECRUITER(3, "ziprecruiter"),;

        private final Integer code;
        private final String name;

        PlatformType(Integer code, String name) {
            this.code = code;
            this.name = name;
        }

        /**
         * Get enum by code
         */
        public static PlatformType getByCode(Integer code) {
            for (PlatformType type : values()) {
                if (type.code.equals(code)) {
                    return type;
                }
            }
            return null;
        }

        /**
         * Get name by code
         */
        public static String getNameByCode(Integer code) {
            PlatformType type = getByCode(code);
            return type != null ? type.getName() : "Unknown Platform";
        }
    }

    /**
     * Update type enum
     */
    @Getter
    public enum UpdateType {
        /** Manual Update */
        MANUAL(1, "Manual Update"),
        /** Auto Update */
        AUTO(2, "Auto Update");

        private final Integer code;
        private final String name;

        UpdateType(Integer code, String name) {
            this.code = code;
            this.name = name;
        }

        /**
         * Get enum by code
         */
        public static UpdateType getByCode(Integer code) {
            for (UpdateType type : values()) {
                if (type.code.equals(code)) {
                    return type;
                }
            }
            return null;
        }

        /**
         * Get name by code
         */
        public static String getNameByCode(Integer code) {
            UpdateType type = getByCode(code);
            return type != null ? type.getName() : "Unknown Type";
        }
    }

    /**
     * Update status enum
     */
    @Getter
    public enum UpdateStatus {
        /** Success */
        SUCCESS(1, "Success"),
        /** Failed */
        FAILED(2, "Failed");

        private final Integer code;
        private final String name;

        UpdateStatus(Integer code, String name) {
            this.code = code;
            this.name = name;
        }

        /**
         * Get enum by code
         */
        public static UpdateStatus getByCode(Integer code) {
            for (UpdateStatus status : values()) {
                if (status.code.equals(code)) {
                    return status;
                }
            }
            return null;
        }

        /**
         * Get name by code
         */
        public static String getNameByCode(Integer code) {
            UpdateStatus status = getByCode(code);
            return status != null ? status.getName() : "Unknown Status";
        }
    }

    /**
     * Business constants
     */
    public static class Business {
        /** Manual update cooldown time (minutes) */
        public static final Integer MANUAL_UPDATE_COOLDOWN_MINUTES = 2;
        
        /** Default update interval hours */
        public static final Integer DEFAULT_UPDATE_INTERVAL_HOURS = 24;
    }
}