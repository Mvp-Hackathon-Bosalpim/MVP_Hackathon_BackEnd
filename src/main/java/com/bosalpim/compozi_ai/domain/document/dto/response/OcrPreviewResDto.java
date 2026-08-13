package com.bosalpim.compozi_ai.domain.document.dto.response;

import com.bosalpim.compozi_ai.domain.document.dto.request.commonFile.CreateCommonItemDocumentReqDto;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OcrPreviewResDto {

    @JsonProperty(value = "doc_id")
    private String docId;       // 문서 ID 추가 (예: TEST-MI-20260801-01)

    @JsonProperty(value = "row_no")
    private Long rowNo;

    @JsonProperty(value = "supplier_name")
    private String supplierName;

    @JsonProperty(value = "raw_item_name")
    private String rawItemName;

    @JsonProperty(value = "source_type")
    private String sourceType;

    @JsonProperty(value = "spec")
    private String spec;

    @JsonProperty(value = "unit")
    private String unit;

    @JsonProperty(value = "price_before")
    private Long priceBefore;

    @JsonProperty(value = "price_after")
    private Long priceAfter;

    @JsonProperty(value = "effective_date")
    private LocalDate effectiveDate;

    public static OcrPreviewResDto from(CreateCommonItemDocumentReqDto dto) {

        return OcrPreviewResDto.builder()
                .docId(dto.getDocId())
                .rowNo(dto.getRowNo())
                .sourceType(dto.getSourceType())
                .supplierName(dto.getSupplierName())
                .rawItemName(dto.getRawItemName())
                .spec(dto.getSpec())
                .unit(dto.getUnit())
                .priceBefore(dto.getPriceBefore())
                .priceAfter(dto.getPriceAfter())
                .effectiveDate(dto.getEffectiveDate())
                .build();
    }
}
