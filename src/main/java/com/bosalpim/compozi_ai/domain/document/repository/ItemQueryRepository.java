package com.bosalpim.compozi_ai.domain.document.repository;


import com.bosalpim.compozi_ai.domain.document.entity.Item;
import java.time.LocalDate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ItemQueryRepository {
    Page<Item> searchItems(String itemName, String supplierName, LocalDate startDate, LocalDate endDate,
                           Pageable pageable);
}
