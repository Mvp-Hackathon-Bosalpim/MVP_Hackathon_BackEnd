package com.bosalpim.compozi_ai.domain.document.component.mapper.rule;

import java.util.List;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Order(3)
@Component
public class SpacingRule implements ItemNormalizationRule {
    // 띄어 쓰기 규칙
    private static final List<String> KEYWORDS = List.of(
            "슬라이스", "쉬레드", "정육", "또띠아", "인치", "치즈", "리드", "보울", "새우살", "살사", "바비큐소스"
    );

    @Override
    public String apply(String input) {
        String result = input;

        for (String keyword : KEYWORDS) {
            result = result.replaceAll("(?<=[^\\s])" + keyword, " " + keyword);
        }

        result = result.replaceAll("(?<=[가-힣a-zA-Z])(?=\\d)", " ");
        result = result.replaceAll("(?<=\\d)(?=[가-힣a-zA-Z])", " ");

        return result.replaceAll("\\s+", " ").trim();
    }
}
