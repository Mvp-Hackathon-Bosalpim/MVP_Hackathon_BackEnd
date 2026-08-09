package com.bosalpim.compozi_ai.domain.inbox.dto.request;


import com.bosalpim.compozi_ai.domain.document.entity.Item;
import java.time.LocalDate;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ItemSnapshotDto {

    private String supplierName;
    private String normalizedItemName;
    private Long priceBefore;
    private Long priceAfter;
    private String spec;
    private String unit;
    private LocalDate effectiveDate;


    public static ItemSnapshotDto create(Item item) {
        return ItemSnapshotDto.builder()
                .supplierName(item.getSupplierName())
                .normalizedItemName(item.getNormalizedItemName())
                .priceBefore(item.getPriceBefore())
                .priceAfter(item.getPriceAfter())
                .spec(item.getSpec())
                .unit(item.getUnit())
                .effectiveDate(item.getEffectiveDate())
                .build();
    }
}
