package com.bosalpim.compozi_ai.domain.inbox.repository;

import com.bosalpim.compozi_ai.domain.inbox.entity.DuplicatedGroup;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DuplicatedGroupRepository extends JpaRepository<DuplicatedGroup, Long> {
}
