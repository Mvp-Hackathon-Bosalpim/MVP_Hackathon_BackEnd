package com.bosalpim.compozi_ai.domain.inbox.repository.change_log;

import com.bosalpim.compozi_ai.domain.inbox.entity.ChangeLog;
import com.bosalpim.compozi_ai.domain.inbox.enums.Action;
import java.util.List;

public interface ChangeLogQueryRepository {
    List<ChangeLog> findAllByItemId(Long itemId);

    List<ChangeLog> findByItemIdInAndAction(List<Long> itemIds, Action action);
}
