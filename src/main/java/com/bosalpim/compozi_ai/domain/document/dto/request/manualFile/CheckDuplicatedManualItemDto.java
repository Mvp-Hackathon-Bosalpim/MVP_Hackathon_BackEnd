package com.bosalpim.compozi_ai.domain.document.dto.request.manualFile;

import java.time.LocalDate;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Builder
public class CheckDuplicatedManualItemDto {
    private String supplierName;

    private String normalizedItemName;

    private String spec;

    private String unit;

    private Long priceBefore;

    private Long priceAfter;

    private LocalDate effectiveDate;

    @Setter
    private String duplicateGroupKey;

    public static CheckDuplicatedManualItemDto create(CreateManualItemDocumentReqDto dto, String normalizedItemName) {
        return CheckDuplicatedManualItemDto.builder()
                .supplierName(dto.getSupplierName())
                .normalizedItemName(normalizedItemName)
                .spec(dto.getSpec())
                .unit(dto.getUnit())
                .priceBefore(dto.getPriceBefore())
                .priceAfter(dto.getPriceAfter())
                .effectiveDate(dto.getEffectiveDate())
                .build();

    }

}
