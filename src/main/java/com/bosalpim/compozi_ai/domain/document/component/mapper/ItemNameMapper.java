package com.bosalpim.compozi_ai.domain.document.component.mapper;

import com.bosalpim.compozi_ai.domain.document.component.mapper.dictionary.ItemDictionary;
import com.bosalpim.compozi_ai.domain.document.component.mapper.rule.ItemNormalizationRule;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ItemNameMapper {
    private final ItemDictionary itemDictionary;
    private final List<ItemNormalizationRule> rules;

    public String map(String rawName) {

        if (rawName == null || rawName.isBlank()) {
            return "데이터 부족";
        }

        Optional<String> exactMatch = itemDictionary.findMappedName(rawName);
        if (exactMatch.isPresent()) {
            return exactMatch.get();
        }

        String normalized = rawName;

        for (ItemNormalizationRule rule : rules) {
            normalized = rule.apply(normalized);
        }
// @Deprecated
//        if (rawName.equals(normalized)) {
//            Optional<String> aiResult = aiService.normalizeWithAi(rawName);
//            if (aiResult.isPresent() && !aiResult.get().isBlank()) {
//                return aiResult.get(); // AI 정규화 결과 사용
//            }
//        }

        if (rawName.equals(normalized)) {
            return "데이터 부족";
        }

        return normalized;
    }


}
