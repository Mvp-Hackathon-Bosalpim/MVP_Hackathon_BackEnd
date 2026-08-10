package com.bosalpim.compozi_ai.domain.inbox.repository.issue;

import com.bosalpim.compozi_ai.domain.inbox.entity.Issue;
import java.util.List;

public interface IssueQueryRepository {
    List<Issue> findUnresolvedByItemId(Long itemId);
}
