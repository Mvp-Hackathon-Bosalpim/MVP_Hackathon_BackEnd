package com.bosalpim.compozi_ai.domain.dashboard.dto;

import com.bosalpim.compozi_ai.domain.inbox.enums.IssueType;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class IssueTypeCountDto {
    private final IssueType issueType;
    private final Long count;
}
