package com.bosalpim.compozi_ai.domain.inbox.dto.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class StatusCountResponseDto {
    private long newCount;
    private long needsReviewCount;
    private long onHoldCount;
    private long approvedCount;
    private long rejectedCount;
}
