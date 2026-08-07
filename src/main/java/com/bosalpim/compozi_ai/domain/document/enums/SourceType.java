package com.bosalpim.compozi_ai.domain.document.enums;

import com.bosalpim.compozi_ai.general.enums.BadStatusCode;
import com.bosalpim.compozi_ai.general.exception.CustomException;

public enum SourceType {
    PDF,
    XLSX,
    IMAGE,
    MANUAL,
    CSV,
    UNKNOWN;

    public static SourceType from(String value) {
        if (value == null || value.isBlank()) {
            return UNKNOWN;
        }
        if ("수기".equals(value)) {
            return MANUAL;
        }

        try {
            return SourceType.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new CustomException(BadStatusCode.UNSUPPORTED_FILE_TYPE);
        }
    }
}
