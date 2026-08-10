package com.bosalpim.compozi_ai.domain.inbox.dto.response;

import com.bosalpim.compozi_ai.domain.document.enums.SourceType;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(toBuilder = true)
@AllArgsConstructor(access = AccessLevel.PACKAGE)
public class DeletedItemResponseDto {

    private Long id;
    private String docId;
    private SourceType sourceType;
    private String supplierName;
    private String normalizedItemName;
    private String spec;
    private Long priceBefore;
    private Long priceAfter;
    private LocalDate effectiveDate;
    private String memo;

    public DeletedItemResponseDto(Long id, String docId, SourceType sourceType, String supplierName,
                                  String normalizedItemName, String spec, Long priceBefore, Long priceAfter,
                                  LocalDate effectiveDate) {
        this(id, docId, sourceType, supplierName, normalizedItemName, spec, priceBefore, priceAfter,
                effectiveDate, null);
    }
}