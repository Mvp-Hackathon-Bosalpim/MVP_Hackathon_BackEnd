package com.bosalpim.compozi_ai.domain.document.repository;


import com.bosalpim.compozi_ai.domain.document.entity.Item;
import com.bosalpim.compozi_ai.domain.document.enums.ReviewStatus;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ItemQueryRepository {
    Page<Item> searchItems(List<String> itemNames, List<String> supplierNames, LocalDate startDate,
                           LocalDate endDate, ReviewStatus reviewStatus, Pageable pageable);
}
