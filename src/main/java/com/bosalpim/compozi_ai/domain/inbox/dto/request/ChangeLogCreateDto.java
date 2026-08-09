package com.bosalpim.compozi_ai.domain.inbox.dto.request;

import com.bosalpim.compozi_ai.domain.document.entity.Item;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class ChangeLogCreateDto {
    private String fromValue;
    private String toValue;
    private String fieldName;

    public static ChangeLogCreateDto of(String fieldName, Object fromValue, Object toValue) {
        return ChangeLogCreateDto.builder()
                .fieldName(fieldName)
                .fromValue(fromValue != null ? String.valueOf(fromValue) : null) // "null" 이라는 문자열도 처리 가능
                .toValue(toValue != null ? String.valueOf(toValue) : null)
                .build();
    }

    public static List<ChangeLogCreateDto> createList(ItemSnapshotDto beforeItem, Item afterItem) {
        List<ChangeLogCreateDto> logs = new ArrayList<>();

        if (!Objects.equals(beforeItem.getEffectiveDate(), afterItem.getEffectiveDate())) {
            logs.add(ChangeLogCreateDto.of("effective_date", beforeItem.getEffectiveDate(),
                    afterItem.getEffectiveDate()));
        }

        if (!Objects.equals(beforeItem.getPriceBefore(), afterItem.getPriceBefore())) {
            logs.add(ChangeLogCreateDto.of("price_before", beforeItem.getPriceBefore(), afterItem.getPriceBefore()));
        }

        if (!Objects.equals(beforeItem.getPriceAfter(), afterItem.getPriceAfter())) {
            logs.add(ChangeLogCreateDto.of("price_after", beforeItem.getPriceAfter(), afterItem.getPriceAfter()));
        }

        if (!Objects.equals(beforeItem.getNormalizedItemName(), afterItem.getNormalizedItemName())) {
            logs.add(ChangeLogCreateDto.of("normalized_item_name", beforeItem.getNormalizedItemName(),
                    afterItem.getNormalizedItemName()));
        }

        if (!Objects.equals(beforeItem.getSupplierName(), afterItem.getSupplierName())) {
            logs.add(ChangeLogCreateDto.of("supplier_name", beforeItem.getSupplierName(), afterItem.getSupplierName()));
        }

        if (!Objects.equals(beforeItem.getSpec(), afterItem.getSpec())) {
            logs.add(ChangeLogCreateDto.of("spec", beforeItem.getSpec(), afterItem.getSpec()));
        }

        if (!Objects.equals(beforeItem.getUnit(), afterItem.getUnit())) {
            logs.add(ChangeLogCreateDto.of("unit", beforeItem.getUnit(), afterItem.getUnit()));
        }

        return logs;
    }
}
