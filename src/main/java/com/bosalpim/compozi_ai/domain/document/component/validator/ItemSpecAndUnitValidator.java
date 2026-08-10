package com.bosalpim.compozi_ai.domain.document.component.validator;

import com.bosalpim.compozi_ai.domain.document.component.parser.ValidItemSpecAndUnit;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Set;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

@Component
public class ItemSpecAndUnitValidator implements
        ConstraintValidator<ValidItemSpecAndUnit, SpecAndUnitAware> {

    private static final Pattern SPEC_CHANGE_PATTERN =
            Pattern.compile("^\\s*기존\\s*(?<old>.+?)\\s*/\\s*변경\\s*(?<new>.+?)\\s*$");

    // 표준 단위 집합
    private static final Set<String> ALLOWED_UNITS = Set.of("PK", "BOX", "EA", "PO");

    public boolean isSpecMismatch(String spec) {
        if (spec == null || spec.isBlank()) {
            return false;
        }
        return SPEC_CHANGE_PATTERN.matcher(spec.trim()).matches();
    }

    public boolean isUnitMismatch(String unit) {
        if (unit == null || unit.isBlank()) {
            return false;
        }
        String trimmedUnit = unit.trim().toUpperCase();

        if (trimmedUnit.contains("/") || trimmedUnit.contains(",")) {
            return true;
        }
        return !ALLOWED_UNITS.contains(trimmedUnit);
    }

    @Override
    public boolean isValid(SpecAndUnitAware dto, ConstraintValidatorContext context) {
        if (dto == null) {
            return true;
        }

        String spec = dto.getSpec();
        String unit = dto.getUnit();

        if (isSpecMismatch(spec)) {
            return false;
        }

        if (isUnitMismatch(unit)) {
            return false;
        }

        return true;
    }
}
