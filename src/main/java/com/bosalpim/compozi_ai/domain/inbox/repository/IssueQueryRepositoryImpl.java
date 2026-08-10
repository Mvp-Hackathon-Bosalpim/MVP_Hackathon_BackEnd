package com.bosalpim.compozi_ai.domain.inbox.repository;

import static com.bosalpim.compozi_ai.domain.inbox.entity.QIssue.issue;

import com.bosalpim.compozi_ai.domain.inbox.entity.Issue;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class IssueQueryRepositoryImpl implements IssueQueryRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<Issue> findUnresolvedByItemId(Long itemId) {
        return queryFactory.selectFrom(issue)
                .where(issue.item.id.eq(itemId), issue.resolved.isFalse())
                .fetch();
    }
}
