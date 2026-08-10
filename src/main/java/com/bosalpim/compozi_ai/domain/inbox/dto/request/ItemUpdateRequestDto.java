package com.bosalpim.compozi_ai.domain.inbox.dto.request;

import com.bosalpim.compozi_ai.domain.document.component.parser.ValidItemSpecAndUnit;
import com.bosalpim.compozi_ai.domain.document.component.validator.SpecAndUnitAware;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import java.time.LocalDate;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@ValidItemSpecAndUnit
public class ItemUpdateRequestDto implements SpecAndUnitAware {

    @JsonProperty("normalized_item_name")
    String normalizedItemName;

    @JsonProperty("supplier_name")
    String supplierName;

    String spec;
    String unit;

    @JsonProperty("price_before")
    @Min(value = 0, message = "변경 전 가격은 0원 이상이어야 합니다.")
    Long priceBefore;

    @JsonProperty("price_after")
    @Min(value = 0, message = "변경 후 가격은 0원 이상이어야 합니다.")
    Long priceAfter;

    @JsonProperty("effective_date")
    LocalDate effectiveDate;
}
