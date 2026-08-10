package com.bosalpim.compozi_ai.domain.inbox.repository.change_log;

import com.bosalpim.compozi_ai.domain.inbox.entity.ChangeLog;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChangeLogRepository extends JpaRepository<ChangeLog, Long>, ChangeLogQueryRepository {
    List<ChangeLog> findAllByItemIdIn(List<Long> itemIds);
}
