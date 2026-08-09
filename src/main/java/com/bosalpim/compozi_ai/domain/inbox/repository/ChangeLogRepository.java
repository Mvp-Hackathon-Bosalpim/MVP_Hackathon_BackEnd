package com.bosalpim.compozi_ai.domain.inbox.repository;

import com.bosalpim.compozi_ai.domain.inbox.entity.ChangeLog;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChangeLogRepository extends JpaRepository<ChangeLog, Long> {
    List<ChangeLog> findAllByItemId(Long itemId);

    List<ChangeLog> findAllByItemIdIn(List<Long> itemIds);
}
