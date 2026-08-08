package com.bosalpim.compozi_ai.domain.inbox.repository;

import com.bosalpim.compozi_ai.domain.inbox.entity.Issue;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IssueRepository extends JpaRepository<Issue, Long> {
    boolean existsByItemIdAndResolvedFalse(Long itemId);

    List<Issue> findByItemIdInAndResolvedFalse(List<Long> itemIds);

    List<Issue> findByItemIdAndResolvedFalse(Long itemId);
}
