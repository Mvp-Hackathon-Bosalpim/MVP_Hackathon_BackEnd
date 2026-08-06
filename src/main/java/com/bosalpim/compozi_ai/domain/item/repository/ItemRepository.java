package com.bosalpim.compozi_ai.domain.item.repository;

import com.bosalpim.compozi_ai.domain.item.entity.Item;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemRepository extends JpaRepository<Item, Long> {
}
