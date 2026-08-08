package com.bosalpim.compozi_ai.domain.inbox.dto.response;

import com.bosalpim.compozi_ai.domain.document.entity.Item;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ItemListResponseDto {
    private Long id;
    private String docId;
    private String sourceType;
    private String supplierName;
    private String normalizedItemName;
    private String rawItemName;
    private String spec;
    private String unit;
    private Long priceBefore;
    private Long priceAfter;
    private LocalDate effectiveDate;
    private String reviewStatus;

    public static ItemListResponseDto from(Item item) {
        return new ItemListResponseDto(
                item.getId(),
                item.getDocId(),
                item.getSourceType().name(),
                item.getSupplierName(),
                item.getNormalizedItemName(),
                item.getRawItemName(),
                item.getSpec(),
                item.getUnit(),
                item.getPriceBefore(),
                item.getPriceAfter(),
                item.getEffectiveDate(),
                item.getReviewStatus().name()
        );
    }

}
