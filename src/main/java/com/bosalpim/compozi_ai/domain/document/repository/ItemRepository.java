package com.bosalpim.compozi_ai.domain.document.repository;

import com.bosalpim.compozi_ai.domain.document.entity.Item;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemRepository extends JpaRepository<Item, Long> {
    Page<Item> findByDeletedAtIsNull(Pageable pageable);
}
