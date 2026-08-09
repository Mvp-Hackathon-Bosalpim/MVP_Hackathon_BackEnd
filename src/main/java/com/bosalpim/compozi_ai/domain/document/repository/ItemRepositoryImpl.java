package com.bosalpim.compozi_ai.domain.document.repository;

import static com.bosalpim.compozi_ai.domain.document.entity.QItem.item;

import com.bosalpim.compozi_ai.domain.document.entity.Item;
import com.bosalpim.compozi_ai.domain.document.enums.ReviewStatus;
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
    public Page<Item> searchItems(List<String> itemNames, List<String> supplierNames, LocalDate startDate,
                                  LocalDate endDate, ReviewStatus reviewStatus, Pageable pageable) {

        List<Item> content = queryFactory
                .selectFrom(item)
                .where(
                        item.deletedAt.isNull(),
                        itemNameIn(itemNames),
                        supplierNameIn(supplierNames),
                        effectiveDateBetween(startDate, endDate),
                        reviewStatusEq(reviewStatus)
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
                        itemNameIn(itemNames),
                        supplierNameIn(supplierNames),
                        effectiveDateBetween(startDate, endDate),
                        reviewStatusEq(reviewStatus)
                )
                .fetchOne();

        return new PageImpl<>(content, pageable, total != null ? total : 0);
    }

    private BooleanExpression itemNameIn(List<String> itemNames) {
        return (itemNames == null || itemNames.isEmpty()) ? null : item.normalizedItemName.in(itemNames);
    }

    private BooleanExpression supplierNameIn(List<String> supplierNames) {
        return (supplierNames == null || supplierNames.isEmpty()) ? null : item.supplierName.in(supplierNames);
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

    private BooleanExpression reviewStatusEq(ReviewStatus reviewStatus) {
        return reviewStatus == null ? null : item.reviewStatus.eq(reviewStatus);
    }
}