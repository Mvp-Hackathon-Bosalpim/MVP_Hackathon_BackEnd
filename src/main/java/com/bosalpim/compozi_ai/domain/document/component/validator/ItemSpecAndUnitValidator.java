package com.bosalpim.compozi_ai.domain.document.component.validator;

import java.util.Set;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

@Component
public class ItemSpecAndUnitValidator { // 규격 체킹

    // 규격 정규식 패턴
    private static final Pattern SPEC_CHANGE_PATTERN =
            Pattern.compile("^\\s*기존\\s*(?<old>.+?)\\s*/\\s*변경\\s*(?<new>.+?)\\s*$");

    // 표준 단위 집합
    private static final Set<String> ALLOWED_UNITS = Set.of("PK", "BOX", "EA", "PO");


    public boolean isSpecMismatch(String spec) {
        // 수행 이전에 빈 필드 체킹 과정 있음
        return SPEC_CHANGE_PATTERN.matcher(spec).matches();
    }

    public boolean isUnitMismatch(String unit) {
        String trimmedUnit = unit.trim().toUpperCase();

        if (trimmedUnit.contains("/") || trimmedUnit.contains(",")) {
            return true;
        }
        return !ALLOWED_UNITS.contains(trimmedUnit);
    }


}
