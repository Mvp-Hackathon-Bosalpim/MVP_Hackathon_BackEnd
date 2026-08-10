package com.bosalpim.compozi_ai.domain.inbox.repository;

import com.bosalpim.compozi_ai.domain.inbox.entity.ChangeLog;
import java.util.List;

public interface ChangeLogQueryRepository {
    List<ChangeLog> findAllByItemId(Long itemId);
}
