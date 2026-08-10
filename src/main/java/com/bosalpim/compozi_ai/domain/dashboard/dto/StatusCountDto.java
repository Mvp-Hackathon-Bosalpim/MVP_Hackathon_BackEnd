package com.bosalpim.compozi_ai.domain.dashboard.dto;

import com.bosalpim.compozi_ai.domain.document.enums.ReviewStatus;
import lombok.AllArgsConstructor;
import lombok.Data;


@Data
@AllArgsConstructor
public class StatusCountDto {
    private final ReviewStatus reviewStatus;
    private final Long totalCount;
    private final Long todayCount;
}
