package com.bosalpim.compozi_ai.domain.inbox.repository.issue;

import static com.bosalpim.compozi_ai.domain.inbox.entity.QIssue.issue;

import com.bosalpim.compozi_ai.domain.dashboard.dto.IssueSummaryDto;
import com.bosalpim.compozi_ai.domain.dashboard.dto.IssueTypeCountDto;
import com.bosalpim.compozi_ai.domain.inbox.entity.Issue;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDateTime;
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

    @Override
    public IssueSummaryDto countUnresolvedItemsForDashboard(LocalDateTime todayStart) {
        return queryFactory
                .select(Projections.constructor(IssueSummaryDto.class,
                        issue.item.id.countDistinct(),
                        new CaseBuilder()
                                .when(issue.createdAt.goe(todayStart)).then(issue.item.id)
                                .otherwise((Long) null)
                                .countDistinct()
                ))
                .from(issue)
                .where(issue.resolved.isFalse())
                .fetchOne();
    }

    @Override
    public List<IssueTypeCountDto> countByIssueType() {
        return queryFactory
                .select(Projections.constructor(IssueTypeCountDto.class,
                        issue.issueType,
                        issue.count()))
                .from(issue)
                .where(issue.resolved.isFalse())
                .groupBy(issue.issueType)
                .fetch();
    }
}
