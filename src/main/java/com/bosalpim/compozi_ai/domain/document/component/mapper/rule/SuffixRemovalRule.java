package com.bosalpim.compozi_ai.domain.document.component.mapper.rule;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Order(2)
@Component
public class SuffixRemovalRule implements ItemNormalizationRule {

    // 단위 패턴 제거

    private static final Pattern SUFFIX_PATTERN =
            Pattern.compile("(\\d+(\\.\\d+)?\\s*(입|과|[kK]|[gG]|ml|단|통|봉|포)?|\\d{3,4})$");

    @Override
    public String apply(String input) {
        Matcher matcher = SUFFIX_PATTERN.matcher(input.trim());
        return matcher.replaceAll("").trim();
    }
}
