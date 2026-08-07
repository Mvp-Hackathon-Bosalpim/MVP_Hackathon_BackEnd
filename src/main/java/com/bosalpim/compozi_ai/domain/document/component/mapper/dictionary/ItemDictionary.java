package com.bosalpim.compozi_ai.domain.document.component.mapper.dictionary;

import com.bosalpim.compozi_ai.general.enums.BadStatusCode;
import com.bosalpim.compozi_ai.general.exception.CustomException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ItemDictionary {

    private final Map<String, String> dictionary = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper;

    @PostConstruct
    public void init() {
        loadDictionary();
    }

    private void loadDictionary() {
        try {
            ClassPathResource resource = new ClassPathResource("dictionary/item-dictionary.json");
            Map<String, String> loadedData = objectMapper.readValue(
                    resource.getInputStream(),
                    new TypeReference<>() {
                    }
            );

            dictionary.clear();
            dictionary.putAll(loadedData);
        } catch (IOException e) {
            throw new CustomException(BadStatusCode.ITEM_LOADING_FAIL);
        }
    }

    public Optional<String> findMappedName(String rawName) {
        if (rawName == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(dictionary.get(rawName));
    }
}
