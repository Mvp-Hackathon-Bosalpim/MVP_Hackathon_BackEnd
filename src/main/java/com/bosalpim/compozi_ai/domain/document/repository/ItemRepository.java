package com.bosalpim.compozi_ai.domain.document.repository;

import com.bosalpim.compozi_ai.domain.document.entity.Item;
import com.bosalpim.compozi_ai.domain.document.enums.ReviewStatus;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ItemRepository extends JpaRepository<Item, Long>, ItemQueryRepository {
    Page<Item> findByDeletedAtIsNull(Pageable pageable);

    Long countByReviewStatusAndDeletedAtIsNull(ReviewStatus reviewStatus);

    @Query("SELECT DISTINCT i.normalizedItemName FROM Item i WHERE i.deletedAt IS NULL AND i.normalizedItemName IS NOT NULL ORDER BY i.normalizedItemName ASC")
    List<String> findDistinctNormalizedItemNames();

    @Query("SELECT DISTINCT i.supplierName FROM Item i WHERE i.deletedAt IS NULL AND i.supplierName IS NOT NULL ORDER BY i.supplierName ASC")
    List<String> findDistinctSupplierNames();


    List<Item> findAllByDeletedAtIsNullOrderByIdAsc();

    List<Item> findByFileIdOrderByIdAsc(Long id);

    @Query("SELECT i FROM Item i JOIN FETCH i.file WHERE i.reviewStatus = :reviewStatus AND i.deletedAt IS NULL")
    List<Item> findAllByReviewStatusWithFile(@Param("reviewStatus") ReviewStatus reviewStatus);
}
