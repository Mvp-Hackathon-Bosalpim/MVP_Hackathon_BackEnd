package com.bosalpim.compozi_ai.domain.inbox.dto.request;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.time.LocalDate;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ItemUpdateRequestDto {
    
    String normalizedItemName;
    String supplierName;
    String spec;
    String unit;
    Long priceBefore;
    Long priceAfter;
    LocalDate effectiveDate;
}
