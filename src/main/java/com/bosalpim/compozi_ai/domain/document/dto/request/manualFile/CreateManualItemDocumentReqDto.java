package com.bosalpim.compozi_ai.domain.document.dto.request.manualFile;

import com.bosalpim.compozi_ai.domain.document.component.parser.ValidItemSpecAndUnit;
import com.bosalpim.compozi_ai.domain.document.component.validator.SpecAndUnitAware;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ValidItemSpecAndUnit
public class CreateManualItemDocumentReqDto implements SpecAndUnitAware {

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
    @Min(value = 0, message = "변경 전 가격은 0원 이상이어야 합니다.")
    private Long priceBefore;

    @JsonProperty("price_after")
    @NotNull(message = "변경 후 단가가 누락되었습니다.")
    @Min(value = 0, message = "변경 후 가격은 0원 이상이어야 합니다.")
    private Long priceAfter;

    @JsonProperty("effective_date")
    @NotNull(message = "적용일이 누락되었습니다.")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate effectiveDate;

    //   공백 문자 또는 빈칸이 들어오면 null 처리시키고 @NotNull 유도

    @JsonSetter(value = "supplier_name", nulls = Nulls.AS_EMPTY)
    public void setSupplierName(String supplierName) {
        this.supplierName = trimToNull(supplierName);
    }

    @JsonSetter(value = "raw_item_name", nulls = Nulls.AS_EMPTY)
    public void setRawItemName(String rawItemName) {
        this.rawItemName = trimToNull(rawItemName);
    }

    @JsonSetter(nulls = Nulls.AS_EMPTY)
    public void setSpec(String spec) {
        this.spec = trimToNull(spec);
    }

    @JsonSetter(nulls = Nulls.AS_EMPTY)
    public void setUnit(String unit) {
        this.unit = trimToNull(unit);
    }

    private String trimToNull(String str) {
        if (str == null || str.trim().isEmpty()) {
            return null;
        }
        return str.trim();
    }
}
