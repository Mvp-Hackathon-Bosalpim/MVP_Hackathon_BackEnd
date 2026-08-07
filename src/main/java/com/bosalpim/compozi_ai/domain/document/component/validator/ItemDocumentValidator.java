package com.bosalpim.compozi_ai.domain.document.component.validator;

import com.bosalpim.compozi_ai.domain.document.component.mapper.ItemNameMapper;
import com.bosalpim.compozi_ai.domain.document.dto.request.commonFile.CreateCommonItemDocumentReqDto;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ItemDocumentValidator {

    private final ItemNameMapper itemNameMapper;

    public void markDuplicates(List<CreateCommonItemDocumentReqDto> dtos) {

        Map<String, CreateCommonItemDocumentReqDto> firstSeenMap = new HashMap<>();

        for (CreateCommonItemDocumentReqDto dto : dtos) {

            String normalizedName = itemNameMapper.map(dto.getRawItemName());
            dto.setNormalizedItemName(normalizedName);
            String key = generateKey(dto, normalizedName);

            if (firstSeenMap.containsKey(key)) {

                CreateCommonItemDocumentReqDto firstDto = firstSeenMap.get(key);
                if (firstDto.getDuplicateGroupKey() == null) {
                    firstDto.setDuplicateGroupKey(key);
                }

                dto.setDuplicateGroupKey(key);

            } else {
                firstSeenMap.put(key, dto);
            }
        }

    }

    private String generateKey(CreateCommonItemDocumentReqDto dto, String normalizedName) {
        return String.join("|",
                String.valueOf(dto.getSupplierName()),
                String.valueOf(normalizedName),
                String.valueOf(dto.getSpec()),
                String.valueOf(dto.getUnit()),
                String.valueOf(dto.getPriceBefore()),
                String.valueOf(dto.getPriceAfter()),
                String.valueOf(dto.getEffectiveDate())
        );
    }
}
