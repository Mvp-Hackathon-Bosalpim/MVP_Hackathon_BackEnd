package com.bosalpim.compozi_ai.domain.inbox.dto.response;

import com.bosalpim.compozi_ai.domain.document.enums.SourceType;
import java.time.LocalDate;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(toBuilder = true)
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
}