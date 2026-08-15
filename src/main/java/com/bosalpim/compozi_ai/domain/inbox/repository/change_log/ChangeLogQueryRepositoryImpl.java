package com.bosalpim.compozi_ai.domain.inbox.repository.change_log;

import static com.bosalpim.compozi_ai.domain.inbox.entity.QChangeLog.changeLog;

import com.bosalpim.compozi_ai.domain.inbox.entity.ChangeLog;
import com.bosalpim.compozi_ai.domain.inbox.enums.Action;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ChangeLogQueryRepositoryImpl implements ChangeLogQueryRepository {
    private final JPAQueryFactory queryFactory;

    @Override
    public List<ChangeLog> findAllByItemId(Long itemId) {
        return queryFactory.selectFrom(changeLog)
                .where(changeLog.item.id.eq(itemId))
                .fetch();
    }

    @Override
    public List<ChangeLog> findByItemIdInAndAction(List<Long> itemIds, Action action) {
        return queryFactory.selectFrom(changeLog)
                .where(
                        changeLog.item.id.in(itemIds),
                        changeLog.action.eq(action)
                )
                .fetch();
    }
}
