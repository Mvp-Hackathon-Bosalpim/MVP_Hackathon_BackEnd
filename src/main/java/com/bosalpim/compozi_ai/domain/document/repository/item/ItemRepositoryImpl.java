package com.bosalpim.compozi_ai.domain.document.repository.item;

import static com.bosalpim.compozi_ai.domain.document.entity.QFile.file;
import static com.bosalpim.compozi_ai.domain.document.entity.QItem.item;

import com.bosalpim.compozi_ai.domain.document.entity.Item;
import com.bosalpim.compozi_ai.domain.document.enums.ReviewStatus;
import com.bosalpim.compozi_ai.domain.inbox.dto.response.DeletedItemResponseDto;
import com.bosalpim.compozi_ai.domain.inbox.dto.response.ItemNavigationDto;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.DatePath;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
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

    @Override
    public ItemNavigationDto findNavigationById(Long targetId) {
        Tuple result = queryFactory
                .select(new CaseBuilder().when(item.id.lt(targetId)).then(item.id).otherwise((Long) null).max(),
                        new CaseBuilder().when(item.id.gt(targetId)).then(item.id).otherwise((Long) null).min(),
                        item.count(),
                        new CaseBuilder().when(item.id.lt(targetId)).then(1L).otherwise(0L).sum()
                )
                .from(item)
                .where(item.deletedAt.isNull())
                .fetchOne();

        Long prevId = result.get(0, Long.class);
        Long nextId = result.get(1, Long.class);
        Long totalCount = result.get(2, Long.class);
        Long countBeforeTarget = result.get(3, Long.class);

        return ItemNavigationDto.builder()
                .targetId(targetId)
                .targetIndex(countBeforeTarget != null ? countBeforeTarget + 1L : 1L)
                .prevId(prevId)
                .nextId(nextId)
                .totalCount(totalCount != null ? totalCount : 0L)
                .build();
    }

    @Override
    public List<DeletedItemResponseDto> findDeletedItems() {
        return queryFactory
                .select(Projections.constructor(DeletedItemResponseDto.class,
                        item.id,
                        item.docId,
                        item.sourceType,
                        item.supplierName,
                        item.normalizedItemName,
                        item.spec,
                        item.priceBefore,
                        item.priceAfter,
                        item.effectiveDate))
                .from(item)
                .where(item.deletedAt.isNotNull())
                .fetch();
    }

    @Override
    public Optional<Item> findByIdWithFile(Long id) {
        Item result = queryFactory.selectFrom(item)
                .leftJoin(item.file, file).fetchJoin()
                .where(item.id.eq(id))
                .fetchOne();
        return Optional.ofNullable(result);
    }

    @Override
    public Optional<Item> findDuplicateCandidate(Long excludeItemId, String supplierName, String normalizedItemName,
                                                 String spec, String unit, Long priceBefore, Long priceAfter,
                                                 LocalDate effectiveDate) {

        Item result = queryFactory.selectFrom(item)
                .where(
                        item.deletedAt.isNull(),
                        item.id.ne(excludeItemId),
                        eqOrIsNull(item.supplierName, supplierName),
                        eqOrIsNull(item.normalizedItemName, normalizedItemName),
                        eqOrIsNull(item.spec, spec),
                        eqOrIsNull(item.unit, unit),
                        eqOrIsNull(item.priceBefore, priceBefore),
                        eqOrIsNull(item.priceAfter, priceAfter),
                        eqOrIsNull(item.effectiveDate, effectiveDate)
                )
                .fetchFirst();

        return Optional.ofNullable(result);
    }

    @Override
    public List<Item> findByDuplicatedGroupIdAndDeletedAtIsNull(Long groupId, Long excludeItemId) {
        return queryFactory.selectFrom(item)
                .where(
                        item.duplicatedGroup.id.eq(groupId),
                        item.deletedAt.isNull(),
                        excludeItemId != null ? item.id.ne(excludeItemId) : null
                )
                .fetch();
    }

    @Override
    public List<Item> findByDuplicatedGroupIdInAndDeletedAtIsNull(Collection<Long> groupIds) {
        return queryFactory.selectFrom(item)
                .where(item.duplicatedGroup.id.in(groupIds), item.deletedAt.isNull())
                .fetch();
    }

    //    == 쿼리 dsl 편의 메서드 == //

    private BooleanExpression eqOrIsNull(StringPath path, String value) {
        return value == null ? path.isNull() : path.eq(value);
    }

    private BooleanExpression eqOrIsNull(NumberPath<Long> path, Long value) {
        return value == null ? path.isNull() : path.eq(value);
    }

    private BooleanExpression eqOrIsNull(DatePath<LocalDate> path, LocalDate value) {
        return value == null ? path.isNull() : path.eq(value);
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
