package com.bosalpim.compozi_ai.domain.document.component.mapper.rule;

import jakarta.annotation.PostConstruct;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Order(1)
@Component
public class AbbreviationRule implements ItemNormalizationRule {

    // 약어 전개
    private final Map<String, String> abbrMap = new LinkedHashMap<>();

    @PostConstruct
    public void init() {
        abbrMap.put("S/O", "소스");
        abbrMap.put("s/o", "소스");
        abbrMap.put("BBQ", "바비큐");
        abbrMap.put("bbq", "바비큐");
        abbrMap.put("냉감튀", "냉동 감자튀김");
        abbrMap.put("돈전지", "돼지고기 전지");
    }


    @Override
    public String apply(String input) {
        if (input == null || input.isBlank()) {
            return input;
        }

        String result = input;
        for (Map.Entry<String, String> entry : abbrMap.entrySet()) {
            result = result.replace(entry.getKey(), entry.getValue());
        }
        return result;
    }
}
