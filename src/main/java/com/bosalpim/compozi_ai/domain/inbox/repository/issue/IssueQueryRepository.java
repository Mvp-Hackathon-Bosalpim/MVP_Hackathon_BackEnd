package com.bosalpim.compozi_ai.domain.inbox.repository.issue;

import com.bosalpim.compozi_ai.domain.dashboard.dto.IssueSummaryDto;
import com.bosalpim.compozi_ai.domain.dashboard.dto.IssueTypeCountDto;
import com.bosalpim.compozi_ai.domain.inbox.entity.Issue;
import java.time.LocalDateTime;
import java.util.List;

public interface IssueQueryRepository {
    List<Issue> findUnresolvedByItemId(Long itemId);

    IssueSummaryDto countUnresolvedItemsForDashboard(LocalDateTime todayStart);

    List<IssueTypeCountDto> countByIssueType();
}
