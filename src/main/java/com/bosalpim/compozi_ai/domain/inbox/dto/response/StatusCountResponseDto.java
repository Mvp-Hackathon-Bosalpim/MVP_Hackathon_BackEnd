package com.bosalpim.compozi_ai.domain.inbox.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class StatusCountResponseDto {
    private long newCount;
    private long needsReviewCount;
    private long onHoldCount;
    private long approvedCount;
    private long rejectedCount;
}
