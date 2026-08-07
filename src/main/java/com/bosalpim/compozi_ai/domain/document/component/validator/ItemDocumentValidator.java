package com.bosalpim.compozi_ai.domain.document.component.validator;

import com.bosalpim.compozi_ai.domain.document.component.mapper.ItemNameMapper;
import com.bosalpim.compozi_ai.domain.document.dto.request.commonFile.CreateCommonItemDocumentReqDto;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ItemDocumentValidator {

    private final ItemNameMapper itemNameMapper;

    public void markDuplicates(List<CreateCommonItemDocumentReqDto> dtos) {
        Set<String> uniqueKeys = new HashSet<>();

        for (CreateCommonItemDocumentReqDto dto : dtos) {
            String normalizedName = itemNameMapper.map(dto.getRawItemName());
            dto.setNormalizedItemName(normalizedName);
            String key = generateKey(dto, normalizedName);
            if (!uniqueKeys.add(key)) {
                dto.setDuplicateGroupKey(key);
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
