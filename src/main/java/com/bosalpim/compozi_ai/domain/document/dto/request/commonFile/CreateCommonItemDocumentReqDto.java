package com.bosalpim.compozi_ai.domain.document.dto.request.commonFile;

import java.time.LocalDate;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CreateCommonItemDocumentReqDto {

    private String docId;

    private String sourceType;

    private Long rowNo;

    private String supplierName;


    private String rawItemName;

    private String spec;

    private String unit;

    private Long priceBefore;

    private Long priceAfter;

    private LocalDate effectiveDate;

}
