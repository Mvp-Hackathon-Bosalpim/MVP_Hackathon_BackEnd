package com.bosalpim.compozi_ai.domain.document.dto.request.manualFile;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CreateManualItemDocumentReqDto {

    @JsonProperty("supplier_name")
    private String supplierName;

    @JsonProperty("raw_item_name")
    private String rawItemName;

    private String spec;
    private String unit;

    @JsonProperty("price_before")
    private Long priceBefore;

    @JsonProperty("price_after")
    private Long priceAfter;

    @JsonProperty("effective_date")
    private LocalDate effectiveDate;

}
