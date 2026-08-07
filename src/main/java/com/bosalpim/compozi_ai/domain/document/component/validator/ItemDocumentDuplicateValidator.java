package com.bosalpim.compozi_ai.domain.document.component.validator;

import com.bosalpim.compozi_ai.domain.document.component.mapper.ItemNameMapper;
import com.bosalpim.compozi_ai.domain.document.dto.request.commonFile.CreateCommonItemDocumentReqDto;
import com.bosalpim.compozi_ai.domain.document.dto.request.manualFile.CheckDuplicatedManualItemDto;
import com.bosalpim.compozi_ai.domain.document.dto.request.manualFile.CreateManualItemDocumentReqDto;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ItemDocumentDuplicateValidator {

    private final ItemNameMapper itemNameMapper;

    public void markDuplicatesForCommon(List<CreateCommonItemDocumentReqDto> dtos) {

        Map<String, CreateCommonItemDocumentReqDto> firstSeenMap = new HashMap<>();

        for (CreateCommonItemDocumentReqDto dto : dtos) {

            String normalizedName = itemNameMapper.map(dto.getRawItemName());
            dto.setNormalizedItemName(normalizedName);
            String key = generateKey(
                    dto.getSupplierName(), normalizedName, dto.getSpec(),
                    dto.getUnit(), dto.getPriceBefore(), dto.getPriceAfter(), dto.getEffectiveDate()
            );

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

    public List<CheckDuplicatedManualItemDto> markDuplicatesForManual(List<CreateManualItemDocumentReqDto> dtos) {
        Map<String, CheckDuplicatedManualItemDto> firstSeenMap = new HashMap<>();

        // DTO 변환 및 키 매핑
        List<CheckDuplicatedManualItemDto> checkDtos = dtos.stream()
                .map(dto -> CheckDuplicatedManualItemDto.create(dto, itemNameMapper.map(dto.getRawItemName())))
                .toList();

        for (CheckDuplicatedManualItemDto checkDto : checkDtos) {
            String key = generateKey(
                    checkDto.getSupplierName(), checkDto.getNormalizedItemName(), checkDto.getSpec(),
                    checkDto.getUnit(), checkDto.getPriceBefore(), checkDto.getPriceAfter(), checkDto.getEffectiveDate()
            );

            if (firstSeenMap.containsKey(key)) {
                CheckDuplicatedManualItemDto firstDto = firstSeenMap.get(key);
                if (firstDto.getDuplicateGroupKey() == null) {
                    firstDto.setDuplicateGroupKey(key);
                }
                checkDto.setDuplicateGroupKey(key);
            } else {
                firstSeenMap.put(key, checkDto);
            }
        }

        return checkDtos;
    }

    private String generateKey(Object... fields) { // 가변인자 활용
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < fields.length; i++) {
            sb.append(fields[i]);
            if (i < fields.length - 1) {
                sb.append("|");
            }
        }
        return sb.toString();
    }
}
