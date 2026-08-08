package com.bosalpim.compozi_ai.domain.document.repository;

import static com.bosalpim.compozi_ai.domain.document.entity.QItem.item;

import com.bosalpim.compozi_ai.domain.document.entity.Item;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

@RequiredArgsConstructor
public class ItemRepositoryImpl implements ItemQueryRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Item> searchItems(String itemName, String supplierName, LocalDate startDate, LocalDate endDate,
                                  Pageable pageable) {

        List<Item> content = queryFactory
                .selectFrom(item)
                .where(
                        item.deletedAt.isNull(),
                        itemNameEq(itemName),
                        supplierNameEq(supplierName),
                        effectiveDateBetween(startDate, endDate)
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(item.id.asc())
                .fetch();

        Long total = queryFactory
                .select(item.count())
                .from(item)
                .where(
                        item.deletedAt.isNull(),
                        itemNameEq(itemName),
                        supplierNameEq(supplierName),
                        effectiveDateBetween(startDate, endDate)
                )
                .fetchOne();

        return new PageImpl<>(content, pageable, total != null ? total : 0);
    }

    private BooleanExpression itemNameEq(String itemName) {
        return (itemName == null || itemName.isBlank()) ? null : item.normalizedItemName.eq(itemName);
    }

    private BooleanExpression supplierNameEq(String supplierName) {
        return (supplierName == null || supplierName.isBlank()) ? null : item.supplierName.eq(supplierName);
    }

    private BooleanExpression effectiveDateBetween(LocalDate startDate, LocalDate endDate) {
        if (startDate == null && endDate == null) {
            return null;
        }
        if (startDate != null && endDate != null) {
            return item.effectiveDate.between(startDate, endDate);
        }
        if (startDate != null) {
            return item.effectiveDate.goe(startDate);
        }
        return item.effectiveDate.loe(endDate);
    }
}