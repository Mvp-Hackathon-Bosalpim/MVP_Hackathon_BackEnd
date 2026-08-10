package com.bosalpim.compozi_ai.domain.document.component.validator;

import com.bosalpim.compozi_ai.domain.document.component.mapper.ItemNameMapper;
import com.bosalpim.compozi_ai.domain.document.dto.request.commonFile.CreateCommonItemDocumentReqDto;
import com.bosalpim.compozi_ai.domain.document.dto.request.manualFile.CheckDuplicatedManualItemDto;
import com.bosalpim.compozi_ai.domain.document.dto.request.manualFile.CreateManualItemDocumentReqDto;
import com.bosalpim.compozi_ai.domain.document.entity.Item;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ItemDocumentDuplicateValidator {

    private final ItemNameMapper itemNameMapper;

    // --- [ Common 파일 중복 검사 ] ---
    public DuplicateValidationResult markDuplicatesForCommon(List<CreateCommonItemDocumentReqDto> dtos,
                                                             List<Item> allExistingItems) {
        if (dtos.isEmpty()) {
            return new DuplicateValidationResult(Map.of(), List.of());
        }

        // DB 기존 데이터 Key -> Item 맵핑
        Map<String, Item> existingDbMap = new HashMap<>();
        for (Item item : allExistingItems) {
            String key = generateKey(
                    item.getSupplierName(), item.getNormalizedItemName(), item.getSpec(),
                    item.getUnit(), item.getPriceBefore(), item.getPriceAfter(), item.getEffectiveDate()
            );
            existingDbMap.putIfAbsent(key, item); // ID가 가장 작은 원본 1개만 유지
        }

        // 현재 입력되는 데이터
        Map<String, CreateCommonItemDocumentReqDto> firstSeenMap = new HashMap<>();
        for (CreateCommonItemDocumentReqDto dto : dtos) {
            String normalizedName = itemNameMapper.map(dto.getRawItemName());
            dto.setNormalizedItemName(normalizedName);

            String key = generateKey(
                    dto.getSupplierName(), normalizedName, dto.getSpec(),
                    dto.getUnit(), dto.getPriceBefore(), dto.getPriceAfter(), dto.getEffectiveDate()
            );

            // DB에 존재하거나 요청 목록 내에서 이미 등장했으면 Duplicate Group Key 부여
            if (existingDbMap.containsKey(key) || firstSeenMap.containsKey(key)) {
                dto.setDuplicateGroupKey(key);

                // 요청 목록 내에서 처음 등장한 DTO에도 그룹 키가 비어있었다면 세팅
                if (firstSeenMap.containsKey(key) && firstSeenMap.get(key).getDuplicateGroupKey() == null) {
                    firstSeenMap.get(key).setDuplicateGroupKey(key);
                }
            } else {
                firstSeenMap.put(key, dto);
            }
        }

        return new DuplicateValidationResult(existingDbMap, dtos);
    }

    // --- [ Manual 파일 중복 검사 ] ---
    public DuplicateValidationResult markDuplicatesForManual(List<CreateManualItemDocumentReqDto> dtos,
                                                             List<Item> allExistingItems) {
        if (dtos.isEmpty()) {
            return new DuplicateValidationResult(Map.of(), List.of());
        }

        // 1. DTO 변환 및 정규화 이름 매핑
        List<CheckDuplicatedManualItemDto> checkDtos = dtos.stream()
                .map(dto -> CheckDuplicatedManualItemDto.create(dto, itemNameMapper.map(dto.getRawItemName())))
                .toList();

        // 2. DB 전체 Item 조회

        Map<String, Item> existingDbMap = new HashMap<>();
        for (Item item : allExistingItems) {
            String key = generateKey(
                    item.getSupplierName(), item.getNormalizedItemName(), item.getSpec(),
                    item.getUnit(), item.getPriceBefore(), item.getPriceAfter(), item.getEffectiveDate()
            );
            existingDbMap.putIfAbsent(key, item);
        }

        Map<String, CheckDuplicatedManualItemDto> firstSeenMap = new HashMap<>();

        for (CheckDuplicatedManualItemDto checkDto : checkDtos) {
            String key = generateKey(
                    checkDto.getSupplierName(), checkDto.getNormalizedItemName(), checkDto.getSpec(),
                    checkDto.getUnit(), checkDto.getPriceBefore(), checkDto.getPriceAfter(), checkDto.getEffectiveDate()
            );

            if (existingDbMap.containsKey(key) || firstSeenMap.containsKey(key)) {
                if (firstSeenMap.containsKey(key)) {
                    CheckDuplicatedManualItemDto firstDto = firstSeenMap.get(key);
                    if (firstDto.getDuplicateGroupKey() == null) {
                        firstDto.setDuplicateGroupKey(key);
                    }
                }
                checkDto.setDuplicateGroupKey(key);
            } else {
                firstSeenMap.put(key, checkDto);
            }
        }

        // second 파라미터로 처리된 checkDtos 반환
        return new DuplicateValidationResult(existingDbMap, checkDtos);
    }

    public String generateKey(Object... fields) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < fields.length; i++) {
            sb.append(fields[i]);
            if (i < fields.length - 1) {
                sb.append("|");
            }
        }
        return sb.toString();
    }

    // --- [ 반환용 DTO/Record ] ---
    public record DuplicateValidationResult(
            Map<String, Item> existingDbMap,
            Object firstSeenInRequestMap // DTO 리스트 또는 맵 형태로 유연하게 받기 위함
    ) {
    }
}
