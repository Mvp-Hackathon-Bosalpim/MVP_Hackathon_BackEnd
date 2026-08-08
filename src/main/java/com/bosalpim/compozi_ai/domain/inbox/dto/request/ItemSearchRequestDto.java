package com.bosalpim.compozi_ai.domain.inbox.dto.request;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ItemSearchRequestDto {
    private String itemName;
    private String supplierName;
    private LocalDate startDate;
    private LocalDate endDate;
}