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
        Optional<String> exactMatch = itemDictionary.findMappedName(rawName);
        if (exactMatch.isPresent()) {
            return exactMatch.get();
        }

        String normalized = rawName;
        for (ItemNormalizationRule rule : rules) {
            normalized = rule.apply(normalized);
        }

        if (rawName.isEmpty() || rawName.equals(normalized)) {
            return "데이터 부족";
        }

        return normalized;
    }


}
