package com.bosalpim.compozi_ai.domain.document.repository.item;

import com.bosalpim.compozi_ai.domain.document.entity.Item;
import com.bosalpim.compozi_ai.domain.document.enums.ReviewStatus;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ItemRepository extends JpaRepository<Item, Long>, ItemQueryRepository {
    Page<Item> findByDeletedAtIsNull(Pageable pageable);

    @Query("SELECT DISTINCT i.normalizedItemName FROM Item i WHERE i.deletedAt IS NULL AND i.normalizedItemName IS NOT NULL ORDER BY i.normalizedItemName ASC")
    List<String> findDistinctNormalizedItemNames();

    @Query("SELECT DISTINCT i.supplierName FROM Item i WHERE i.deletedAt IS NULL AND i.supplierName IS NOT NULL ORDER BY i.supplierName ASC")
    List<String> findDistinctSupplierNames();


    List<Item> findAllByDeletedAtIsNullOrderByIdAsc();


    @Query("SELECT i FROM Item i JOIN FETCH i.file WHERE i.reviewStatus = :reviewStatus AND i.deletedAt IS NULL")
    List<Item> findAllByReviewStatusWithFile(@Param("reviewStatus") ReviewStatus reviewStatus);

    Page<Item> findAllByDuplicatedGroupId(Long groupId, Pageable pageable);

    @Query("SELECT i FROM Item i WHERE i.id = :id AND i.deletedAt IS NULL AND i.reviewStatus NOT IN :excludedStatuses ")
    Optional<Item> findByIdAndDeletedAtIsNull(@Param("id") Long id,
                                              @Param("excludedStatuses") Collection<ReviewStatus> excludedStatuses);

    @Query("SELECT i FROM Item i WHERE i.id IN :targetIds AND i.deletedAt IS NULL AND i.reviewStatus NOT IN :excludedStatuses")
    List<Item> findAllByIdInAndDeletedAtIsNull(@Param("targetIds") List<Long> targetIds,
                                               @Param("excludedStatuses") Collection<ReviewStatus> excludedStatuses);

    @Query("SELECT i.reviewStatus, COUNT(i) FROM Item i WHERE i.deletedAt IS NULL GROUP BY i.reviewStatus")
    List<Object[]> countGroupByReviewStatus();
}
