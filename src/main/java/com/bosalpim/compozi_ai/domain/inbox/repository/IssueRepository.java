package com.bosalpim.compozi_ai.domain.inbox.repository;

import com.bosalpim.compozi_ai.domain.inbox.entity.Issue;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IssueRepository extends JpaRepository<Issue, Long> {
    boolean existsByItemIdAndResolvedFalse(Long itemId);
}
