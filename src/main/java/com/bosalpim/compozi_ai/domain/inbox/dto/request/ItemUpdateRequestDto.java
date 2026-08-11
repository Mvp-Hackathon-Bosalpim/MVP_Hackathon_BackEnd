package com.bosalpim.compozi_ai.domain.inbox.dto.request;

import com.bosalpim.compozi_ai.domain.document.component.parser.ValidItemSpecAndUnit;
import com.bosalpim.compozi_ai.domain.document.component.validator.SpecAndUnitAware;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import jakarta.validation.constraints.Min;
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
public class ItemUpdateRequestDto implements SpecAndUnitAware {

    @JsonProperty("normalized_item_name")
    private String normalizedItemName;

    @JsonProperty("supplier_name")
    private String supplierName;

    private String spec;

    private String unit;

    @JsonProperty("price_before")
    @Min(value = 0, message = "변경 전 가격은 0원 이상이어야 합니다.")
    private Long priceBefore;

    @JsonProperty("price_after")
    @Min(value = 0, message = "변경 후 가격은 0원 이상이어야 합니다.")
    private Long priceAfter;

    @JsonProperty("effective_date")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate effectiveDate;

    // 공백 문자 또는 빈칸이 들어오면 null 처리 -> @NotNull 없으므로 없는 샘 침 (update 니까)

    @JsonSetter(value = "normalized_item_name", nulls = Nulls.AS_EMPTY)
    public void setNormalizedItemName(String normalizedItemName) {
        this.normalizedItemName = trimToNull(normalizedItemName);
    }

    @JsonSetter(value = "supplier_name", nulls = Nulls.AS_EMPTY)
    public void setSupplierName(String supplierName) {
        this.supplierName = trimToNull(supplierName);
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
