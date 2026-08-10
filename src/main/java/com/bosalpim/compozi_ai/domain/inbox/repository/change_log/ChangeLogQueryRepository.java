package com.bosalpim.compozi_ai.domain.inbox.repository.change_log;

import com.bosalpim.compozi_ai.domain.inbox.entity.ChangeLog;
import java.util.List;

public interface ChangeLogQueryRepository {
    List<ChangeLog> findAllByItemId(Long itemId);
}
