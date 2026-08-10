package com.bosalpim.compozi_ai.domain.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class IssueSummaryDto {
    private final Long totalCount;
    private final Long todayCount;
}
