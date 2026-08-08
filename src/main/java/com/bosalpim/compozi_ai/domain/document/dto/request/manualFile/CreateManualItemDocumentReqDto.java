package com.bosalpim.compozi_ai.domain.document.dto.request.manualFile;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import lombok.Getter;

@Getter
public class CreateManualItemDocumentReqDto {

    @JsonProperty("supplier_name")
    @NotBlank(message = "공급사명이 누락되었습니다.")
    private String supplierName;

    @JsonProperty("raw_item_name")
    @NotBlank(message = "원본 품목명이 누락되었습니다.")
    private String rawItemName;

    @NotBlank(message = "규격이 누락되었습니다.")
    private String spec;

    @NotBlank(message = "단위가 누락되었습니다.")
    private String unit;

    @JsonProperty("price_before")
    @NotNull(message = "변경 전 단가가 누락되었습니다.")
    private Long priceBefore;

    @JsonProperty("price_after")
    @NotNull(message = "변경 후 단가가 누락되었습니다.")
    private Long priceAfter;

    @JsonProperty("effective_date")
    @NotNull(message = "적용일이 누락되었습니다.")
    private LocalDate effectiveDate;

}
