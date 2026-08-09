package com.bosalpim.compozi_ai.domain.inbox.dto.request;

import com.bosalpim.compozi_ai.domain.document.enums.ReviewStatus;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.time.LocalDate;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ItemSearchRequestDto {
    private List<String> itemNames;
    private List<String> supplierNames;
    private LocalDate startDate;
    private LocalDate endDate;
    private ReviewStatus reviewStatus;
}