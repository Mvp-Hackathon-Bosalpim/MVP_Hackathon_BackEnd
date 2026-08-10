package com.bosalpim.compozi_ai.domain.inbox.service;

import com.bosalpim.compozi_ai.domain.document.component.validator.ItemDocumentDuplicateValidator;
import com.bosalpim.compozi_ai.domain.document.component.validator.ItemSpecAndUnitValidator;
import com.bosalpim.compozi_ai.domain.document.entity.Item;
import com.bosalpim.compozi_ai.domain.document.enums.ReviewStatus;
import com.bosalpim.compozi_ai.domain.document.repository.ItemRepository;
import com.bosalpim.compozi_ai.domain.document.service.ItemService;
import com.bosalpim.compozi_ai.domain.inbox.dto.request.BulkItemDeleteRequestDto;
import com.bosalpim.compozi_ai.domain.inbox.dto.request.ChangeLogCreateDto;
import com.bosalpim.compozi_ai.domain.inbox.dto.request.ItemSnapshotDto;
import com.bosalpim.compozi_ai.domain.inbox.dto.request.ItemUpdateRequestDto;
import com.bosalpim.compozi_ai.domain.inbox.dto.response.BulkActionResponseDto;
import com.bosalpim.compozi_ai.domain.inbox.dto.response.ItemDetailResponseDto;
import com.bosalpim.compozi_ai.domain.inbox.dto.response.ItemListResponseDto;
import com.bosalpim.compozi_ai.domain.inbox.dto.response.ItemNavigationDto;
import com.bosalpim.compozi_ai.domain.inbox.dto.response.StatusCountResponseDto;
import com.bosalpim.compozi_ai.domain.inbox.entity.ChangeLog;
import com.bosalpim.compozi_ai.domain.inbox.entity.DuplicatedGroup;
import com.bosalpim.compozi_ai.domain.inbox.entity.Issue;
import com.bosalpim.compozi_ai.domain.inbox.enums.Action;
import com.bosalpim.compozi_ai.domain.inbox.enums.IssueType;
import com.bosalpim.compozi_ai.domain.inbox.repository.ChangeLogRepository;
import com.bosalpim.compozi_ai.domain.inbox.repository.DuplicatedGroupRepository;
import com.bosalpim.compozi_ai.domain.inbox.repository.IssueRepository;
import com.bosalpim.compozi_ai.general.enums.BadStatusCode;
import com.bosalpim.compozi_ai.general.exception.CustomException;
import com.bosalpim.compozi_ai.general.response.PageResponseDto;
import jakarta.validation.Validator;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InboxService {
    private final ItemRepository itemRepository;
    private final IssueRepository issueRepository;
    private final ChangeLogRepository changeLogRepository;
    private final DuplicatedGroupRepository duplicatedGroupRepository;
    private final ItemService itemService;
    private final ItemSpecAndUnitValidator itemSpecAndUnitValidator;
    private final ItemDocumentDuplicateValidator itemDocumentDuplicateValidator;
    private final Validator validator;


    @Transactional
    public Long approve(Long id, String memo) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new CustomException(BadStatusCode.ITEM_NOT_FOUND));

        if (item.getReviewStatus() == ReviewStatus.APPROVED) {
            throw new CustomException(BadStatusCode.ITEM_ALREADY_APPROVED);
        }

        if (item.getReviewStatus() == ReviewStatus.REJECTED) {
            throw new CustomException(BadStatusCode.ITEM_ALREADY_REJECTED);
        }

        List<Issue> unresolvedIssues = issueRepository.findByItemAndResolved(item, false);

        boolean hasMissingRequired = unresolvedIssues.stream()
                .anyMatch(issue -> issue.getIssueType() == IssueType.MISSING_REQUIRED);
        if (hasMissingRequired) {
            throw new CustomException(BadStatusCode.UNRESOLVED_ISSUE_EXISTS);
        }

        boolean hasOtherUnresolvedIssue = !unresolvedIssues.isEmpty();
        if (hasOtherUnresolvedIssue && (memo == null || memo.isBlank())) {
            throw new CustomException(BadStatusCode.APPROVAL_MEMO_REQUIRED);
        }

        item.approve();
        changeLogRepository.save(ChangeLog.of(item, Action.APPROVE, memo));

        return item.getId();
    }

    @Transactional
    public Long reject(Long id, String memo) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new CustomException(BadStatusCode.ITEM_NOT_FOUND));

        if (item.getReviewStatus() == ReviewStatus.APPROVED) {
            throw new CustomException(BadStatusCode.ITEM_ALREADY_APPROVED);
        }

        if (item.getReviewStatus() == ReviewStatus.REJECTED) {
            throw new CustomException(BadStatusCode.ITEM_ALREADY_REJECTED);
        }

        item.reject();
        changeLogRepository.save(ChangeLog.of(item, Action.REJECT, memo));
        return item.getId();
    }

    @Transactional
    public BulkActionResponseDto bulkApprove(List<Long> ids, String memo) {
        // 쿼리1: id 목록으로 Item 일괄 조회
        List<Item> items = itemRepository.findAllById(ids);
        Map<Long, Item> itemMap = items.stream()
                .collect(Collectors.toMap(Item::getId, item -> item));

        List<Long> itemIds = new ArrayList<>(itemMap.keySet());

        // 쿼리2: 대상 Item들의 미해결 이슈 일괄 조회
        List<Issue> unresolvedIssues = issueRepository.findByItemIdInAndResolvedFalse(itemIds);

        Map<Long, List<Issue>> unresolvedIssuesByItemId = unresolvedIssues.stream()
                .collect(Collectors.groupingBy(issue -> issue.getItem().getId()));

        List<Long> successIds = new ArrayList<>();
        List<BulkActionResponseDto.FailedItemDto> failedList = new ArrayList<>();
        List<ChangeLog> logsToSave = new ArrayList<>();

        boolean memoProvided = memo != null && !memo.isBlank();

        for (Long id : ids) {
            Item item = itemMap.get(id);

            if (item == null) {
                failedList.add(new BulkActionResponseDto.FailedItemDto(id, BadStatusCode.ITEM_NOT_FOUND));
                continue;
            }
            if (item.getReviewStatus() == ReviewStatus.APPROVED) {
                failedList.add(new BulkActionResponseDto.FailedItemDto(id, BadStatusCode.ITEM_ALREADY_APPROVED));
                continue;
            }
            if (item.getReviewStatus() == ReviewStatus.REJECTED) {
                failedList.add(new BulkActionResponseDto.FailedItemDto(id, BadStatusCode.ITEM_ALREADY_REJECTED));
                continue;
            }

            List<Issue> itemIssues = unresolvedIssuesByItemId.getOrDefault(id, List.of());

            boolean hasMissingRequired = itemIssues.stream()
                    .anyMatch(issue -> issue.getIssueType() == IssueType.MISSING_REQUIRED);
            if (hasMissingRequired) {
                failedList.add(new BulkActionResponseDto.FailedItemDto(
                        id, BadStatusCode.UNRESOLVED_ISSUE_EXISTS,
                        itemIssues.stream().map(issue -> issue.getIssueType().name()).toList()));
                continue;
            }

            boolean hasOtherUnresolvedIssue = !itemIssues.isEmpty();
            if (hasOtherUnresolvedIssue && !memoProvided) {
                failedList.add(new BulkActionResponseDto.FailedItemDto(
                        id, BadStatusCode.APPROVAL_MEMO_REQUIRED,
                        itemIssues.stream().map(issue -> issue.getIssueType().name()).toList()));
                continue;
            }

            item.approve();
            logsToSave.add(ChangeLog.of(item, Action.APPROVE, memo));
            successIds.add(id);
        }

        if (successIds.isEmpty()) {
            throw new CustomException(BadStatusCode.ALL_ITEMS_FAILED);
        }

        // 쿼리3,4: 승인 성공 항목들의 ChangeLog 일괄 저장
        // (item.approve()로 인한 UPDATE는 dirty checking으로 트랜잭션 커밋 시 별도 발생)
        changeLogRepository.saveAll(logsToSave);
        return new BulkActionResponseDto(ids.size(), successIds.size(), failedList.size(), successIds, failedList);
    }

    @Transactional
    public BulkActionResponseDto bulkReject(List<Long> ids, String memo) {

        List<Item> items = itemRepository.findAllById(ids);
        Map<Long, Item> itemMap = items.stream()
                .collect(Collectors.toMap(Item::getId, item -> item));

        List<Long> successIds = new ArrayList<>();
        List<BulkActionResponseDto.FailedItemDto> failedList = new ArrayList<>();
        List<ChangeLog> logsToSave = new ArrayList<>();

        for (Long id : ids) {
            Item item = itemMap.get(id);

            if (item == null) {
                failedList.add(new BulkActionResponseDto.FailedItemDto(id, BadStatusCode.ITEM_NOT_FOUND));
                continue;
            }
            if (item.getReviewStatus() == ReviewStatus.APPROVED) {
                failedList.add(new BulkActionResponseDto.FailedItemDto(id, BadStatusCode.ITEM_ALREADY_APPROVED));
                continue;
            }
            if (item.getReviewStatus() == ReviewStatus.REJECTED) {
                failedList.add(new BulkActionResponseDto.FailedItemDto(id, BadStatusCode.ITEM_ALREADY_REJECTED));
                continue;
            }

            item.reject();
            logsToSave.add(ChangeLog.of(item, Action.REJECT, memo));
            successIds.add(id);
        }

        if (successIds.isEmpty()) {
            throw new CustomException(BadStatusCode.ALL_ITEMS_FAILED);
        }

        changeLogRepository.saveAll(logsToSave);

        return new BulkActionResponseDto(ids.size(), successIds.size(), failedList.size(), successIds, failedList);
    }

    @Transactional
    public BulkActionResponseDto bulkReReview(List<Long> ids, String memo) {

        List<Item> items = itemRepository.findAllById(ids);
        Map<Long, Item> itemMap = items.stream()
                .collect(Collectors.toMap(Item::getId, item -> item));

        List<Long> successIds = new ArrayList<>();
        List<BulkActionResponseDto.FailedItemDto> failedList = new ArrayList<>();
        List<ChangeLog> logsToSave = new ArrayList<>();

        for (Long id : ids) {
            Item item = itemMap.get(id);
            if (item == null) {
                failedList.add(new BulkActionResponseDto.FailedItemDto(id, BadStatusCode.ITEM_NOT_FOUND));
                continue;
            }
            if (item.getReviewStatus() != ReviewStatus.APPROVED
                    && item.getReviewStatus() != ReviewStatus.REJECTED) {
                failedList.add(new BulkActionResponseDto.FailedItemDto(id, BadStatusCode.INVALID_STATUS_FOR_RE_REVIEW));
                continue;
            }

            item.reReview();
            logsToSave.add(ChangeLog.of(item, Action.RE_REVIEW, memo));
            successIds.add(id);
        }

        if (successIds.isEmpty()) {
            throw new CustomException(BadStatusCode.ALL_ITEMS_FAILED);
        }

        changeLogRepository.saveAll(logsToSave);

        return new BulkActionResponseDto(ids.size(), successIds.size(), failedList.size(), successIds, failedList);
    }

    @Transactional(readOnly = true)
    public PageResponseDto<ItemListResponseDto> getItems(Pageable pageable) {
        Page<Item> itemPage = itemRepository.findByDeletedAtIsNull(pageable);

        List<Long> itemIds = itemPage.getContent().stream()
                .map(Item::getId)
                .toList();

        List<Issue> issues = issueRepository.findByItemIdInAndResolvedFalse(itemIds);
        Map<Long, List<String>> issueTypesByItemId = issues.stream()
                .collect(Collectors.groupingBy(
                        issue -> issue.getItem().getId(),
                        Collectors.mapping(issue -> issue.getIssueType().name(), Collectors.toList())
                ));

        Page<ItemListResponseDto> page = itemPage.map(item ->
                ItemListResponseDto.from(item, issueTypesByItemId.getOrDefault(item.getId(), List.of()))
        );

        return new PageResponseDto<>(page);
    }

    @Transactional(readOnly = true)
    public StatusCountResponseDto getStatusCounts() {
        long newCount = itemRepository.countByReviewStatusAndDeletedAtIsNull(ReviewStatus.NEW);
        long needsReviewCount = itemRepository.countByReviewStatusAndDeletedAtIsNull(ReviewStatus.NEEDS_REVIEW);
        long onHoldCount = itemRepository.countByReviewStatusAndDeletedAtIsNull(ReviewStatus.ON_HOLD);
        long approvedCount = itemRepository.countByReviewStatusAndDeletedAtIsNull(ReviewStatus.APPROVED);
        long rejectedCount = itemRepository.countByReviewStatusAndDeletedAtIsNull(ReviewStatus.REJECTED);

        return new StatusCountResponseDto(newCount, needsReviewCount, onHoldCount, approvedCount, rejectedCount);
    }

    @Transactional(readOnly = true)
    public List<String> getNormalizedItemNames() {
        return itemRepository.findDistinctNormalizedItemNames();
    }

    @Transactional(readOnly = true)
    public List<String> getSupplierNames() {
        return itemRepository.findDistinctSupplierNames();
    }

    @Transactional(readOnly = true)
    public PageResponseDto<ItemListResponseDto> searchItems(
            List<String> itemNames, List<String> supplierNames, LocalDate startDate, LocalDate endDate,
            ReviewStatus reviewStatus, Pageable pageable) {

        Page<Item> itemPage = itemRepository.searchItems(itemNames, supplierNames, startDate, endDate, reviewStatus,
                pageable);

        List<Long> itemIds = itemPage.getContent().stream()
                .map(Item::getId)
                .toList();

        List<Issue> issues = issueRepository.findByItemIdInAndResolvedFalse(itemIds);
        Map<Long, List<String>> issueTypesByItemId = issues.stream()
                .collect(Collectors.groupingBy(
                        issue -> issue.getItem().getId(),
                        Collectors.mapping(issue -> issue.getIssueType().name(), Collectors.toList())
                ));

        Page<ItemListResponseDto> page = itemPage.map(item ->
                ItemListResponseDto.from(item, issueTypesByItemId.getOrDefault(item.getId(), List.of()))
        );

        return new PageResponseDto<>(page);
    }

    @Transactional(readOnly = true) // TODO : 조회 성능 개선 필요
    public ItemDetailResponseDto getDetailItem(Long id) {
        Item item = itemRepository.findById(id).
                orElseThrow(() -> new CustomException(BadStatusCode.ITEM_NOT_FOUND));
        List<Issue> issues = issueRepository.findByItemIdAndResolvedFalse(item.getId());
        List<IssueType> exceptionFlags = issues.stream()
                .map(Issue::getIssueType)
                .toList();
        List<ChangeLog> changeLog = changeLogRepository.findAllByItemId(item.getId());
        ItemNavigationDto navigationDto = itemRepository.findNavigationByIdExcludingStatuses(item.getId(),
                List.of(ReviewStatus.APPROVED, ReviewStatus.REJECTED)).orElseThrow(
                () -> new CustomException(BadStatusCode.ITEM_NOT_FOUND)
        );

        return ItemDetailResponseDto.of(item, exceptionFlags, changeLog, navigationDto);
    }

    // 아래 코드는 업데이트 시 수정 로직 (item 변경, 중복, 빈 칸, 단위 * 규격 불일치, 탐지 및 이슈화, 그리고 change_log 생성)
    @Transactional
    public Void updateDetailItem(Long id, ItemUpdateRequestDto reqDto) {
        Item item = itemRepository.findById(id).
                orElseThrow(() -> new CustomException(BadStatusCode.ITEM_NOT_FOUND));

        ItemSnapshotDto beforeItem = ItemSnapshotDto.create(item);

        item.updateItem(reqDto);

        List<Item> otherItems = itemRepository.findAllByDeletedAtIsNullOrderByIdAsc().stream()
                .filter(other -> !other.getId().equals(item.getId()))
                .toList();

        String currentKey = itemDocumentDuplicateValidator.generateKey(
                item.getSupplierName(), item.getNormalizedItemName(), item.getSpec(),
                item.getUnit(), item.getPriceBefore(), item.getPriceAfter(), item.getEffectiveDate()
        );

        Item duplicatedTarget = otherItems.stream()
                .filter(other -> !other.getId().equals(item.getId()))
                .filter(other -> currentKey.equals(itemDocumentDuplicateValidator.generateKey(
                        other.getSupplierName(), other.getNormalizedItemName(), other.getSpec(),
                        other.getUnit(), other.getPriceBefore(), other.getPriceAfter(), other.getEffectiveDate()
                )))
                .findFirst()
                .orElse(null);

        boolean isDuplicate = (duplicatedTarget != null);
        handleDuplicatedGroup(item, duplicatedTarget, otherItems);
        boolean hasMissingField = !validator.validate(item).isEmpty();
        ReviewStatus reviewStatus = itemService.determineReviewStatus(item.getSpec(), item.getUnit(), isDuplicate);

        if (reviewStatus.equals(ReviewStatus.NEW) && hasMissingField) {
            reviewStatus = ReviewStatus.NEEDS_REVIEW;
        }
        item.updateReviewStatus(reviewStatus);
        updateItemIssues(item, isDuplicate, hasMissingField);

        List<ChangeLogCreateDto> createDtos = ChangeLogCreateDto.createList(beforeItem, item);
        List<ChangeLog> changeLogs = createDtos.stream()
                .map(dto -> ChangeLog.of(item, Action.EDIT, dto))
                .toList();
        changeLogRepository.saveAll(changeLogs);

        return null;
    }


    private void handleDuplicatedGroup(Item item, Item duplicatedTarget, List<Item> otherItems) {
        DuplicatedGroup previousGroup = item.getDuplicatedGroup();

        if (duplicatedTarget != null) {
            if (duplicatedTarget.getDuplicatedGroup() != null) {
                item.updateDuplicatedGroup(duplicatedTarget.getDuplicatedGroup());
            } else {
                DuplicatedGroup newGroup = DuplicatedGroup.create();
                duplicatedGroupRepository.save(newGroup);
                duplicatedTarget.updateDuplicatedGroup(newGroup);
                item.updateDuplicatedGroup(newGroup);
            }
        } else {
            item.updateDuplicatedGroup(null);

            if (previousGroup != null) {
                List<Item> remainingItemsInGroup = otherItems.stream()
                        .filter(other -> previousGroup.equals(other.getDuplicatedGroup()))
                        .toList();

                if (remainingItemsInGroup.size() == 1) {
                    Item lonelyItem = remainingItemsInGroup.get(0);
                    lonelyItem.updateReviewStatus(ReviewStatus.NEW);
                    lonelyItem.updateDuplicatedGroup(null);

                    issueRepository.findByItemAndResolved(lonelyItem, false).stream()
                            .filter(issue -> issue.getIssueType() == IssueType.DUPLICATE_SUSPECTED)
                            .forEach(Issue::resolve);
                }
            }
        }
    }

    private void updateItemIssues(Item item, boolean isDuplicate, boolean hasMissingField) {
        List<Issue> unresolvedIssues = issueRepository.findByItemAndResolved(item, false);

        boolean isSpecMismatch = itemSpecAndUnitValidator.isSpecMismatch(item.getSpec());
        boolean isUnitMismatch = itemSpecAndUnitValidator.isUnitMismatch(item.getUnit());

        for (Issue issue : unresolvedIssues) {
            if (issue.getIssueType() == IssueType.SPEC_MISMATCH && !isSpecMismatch) {
                issue.resolve();
            }
            if (issue.getIssueType() == IssueType.UNIT_MISMATCH && !isUnitMismatch) {
                issue.resolve();
            }
            if (issue.getIssueType() == IssueType.MISSING_REQUIRED && !hasMissingField) {
                issue.resolve();
            }
            if (issue.getIssueType() == IssueType.DUPLICATE_SUSPECTED && !isDuplicate) {
                issue.resolve();
            }
        }

        List<Issue> newIssues = new ArrayList<>();

        itemService.collectIssuesIfNeeded(item, item.getSpec(), item.getUnit(),
                issue -> {
                    if (hasNoActiveIssue(unresolvedIssues, issue.getIssueType())) {
                        newIssues.add(issue);
                    }
                },
                hasMissingField
        );

        if (isDuplicate && hasNoActiveIssue(unresolvedIssues, IssueType.DUPLICATE_SUSPECTED)) {
            newIssues.add(Issue.create(IssueType.DUPLICATE_SUSPECTED, "중복 의심", false, item));
        }

        if (!newIssues.isEmpty()) {
            issueRepository.saveAll(newIssues);
        }
    }

    private boolean hasNoActiveIssue(List<Issue> unresolvedIssues, IssueType type) {
        return unresolvedIssues.stream().noneMatch(i -> i.getIssueType() == type);
    }

    // 삭제 서비스
    @Transactional
    public Void deleteDetailItem(Long id) {
        Item item = itemRepository.findById(id).orElseThrow(
                () -> new CustomException(BadStatusCode.ITEM_NOT_FOUND)
        );

        handleDuplicatedGroupOnDelete(item);

        issueRepository.deleteByItem(item);
        item.delete();

        ChangeLog changeLog = ChangeLog.of(item, Action.DELETE);
        changeLogRepository.save(changeLog);

        return null;
    }

    private void handleDuplicatedGroupOnDelete(Item deletedItem) {
        DuplicatedGroup targetGroup = deletedItem.getDuplicatedGroup();
        if (targetGroup == null) {
            return;
        }

        Long groupId = targetGroup.getId();

        List<Item> remainingItemsInGroup = itemRepository.findAllByDeletedAtIsNullOrderByIdAsc().stream()
                .filter(other -> !other.getId().equals(deletedItem.getId()))
                .filter(other -> other.getDuplicatedGroup() != null)
                .filter(other -> groupId.equals(other.getDuplicatedGroup().getId()))
                .toList();

        if (remainingItemsInGroup.size() == 1) {
            Item lonelyItem = remainingItemsInGroup.get(0);

            lonelyItem.updateDuplicatedGroup(null);

            issueRepository.findByItemAndResolved(lonelyItem, false).stream()
                    .filter(issue -> issue.getIssueType() == IssueType.DUPLICATE_SUSPECTED)
                    .forEach(Issue::resolve);
        }

        deletedItem.updateDuplicatedGroup(null);
    }

    // 다건 bulk 삭제
    @Transactional
    public List<Long> deleteBulkItem(BulkItemDeleteRequestDto bulkItemDeleteRequestDto) {
        List<Long> targetIds = bulkItemDeleteRequestDto.getIds();
        if (targetIds == null || targetIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<Item> targetItems = itemRepository.findAllById(targetIds);
        if (targetItems.isEmpty()) {
            return Collections.emptyList();
        }

        Set<Long> affectedGroupIds = targetItems.stream()
                .map(Item::getDuplicatedGroup)
                .filter(Objects::nonNull)
                .map(DuplicatedGroup::getId)
                .collect(Collectors.toSet());

        issueRepository.deleteAllByItemIn(targetItems);

        List<ChangeLog> changeLogs = new ArrayList<>();
        for (Item item : targetItems) {
            item.updateDuplicatedGroup(null); // 본인 그룹 연관관계 해제
            item.delete();
            changeLogs.add(ChangeLog.of(item, Action.DELETE));
        }
        changeLogRepository.saveAll(changeLogs);

        if (!affectedGroupIds.isEmpty()) {
            handleDuplicatedGroupsOnBulkDelete(affectedGroupIds);
        }

        return targetItems.stream().map(Item::getId).toList();
    }

    private void handleDuplicatedGroupsOnBulkDelete(Set<Long> affectedGroupIds) {
        List<Item> activeItems = itemRepository.findAllByDeletedAtIsNullOrderByIdAsc();

        for (Long groupId : affectedGroupIds) {
            List<Item> remainingItems = activeItems.stream()
                    .filter(item -> item.getDuplicatedGroup() != null)
                    .filter(item -> groupId.equals(item.getDuplicatedGroup().getId()))
                    .toList();

            if (remainingItems.size() <= 1) {
                for (Item lonelyItem : remainingItems) {
                    lonelyItem.updateDuplicatedGroup(null);

                    issueRepository.findByItemAndResolved(lonelyItem, false).stream()
                            .filter(issue -> issue.getIssueType() == IssueType.DUPLICATE_SUSPECTED)
                            .forEach(Issue::resolve);
                }
            }
        }
    }

    // 중복 그룹 id 를 통한 중복 아이템 조회
    @Transactional(readOnly = true)
    public PageResponseDto<ItemListResponseDto> searchItemsWithDuplicatedGroupId(Long groupId, Pageable pageable) {

        Page<Item> itemPage = itemRepository.findAllByDuplicatedGroupId(groupId, pageable);
        List<Item> items = itemPage.getContent();

        if (items.isEmpty()) {
            return new PageResponseDto<>(Page.empty(pageable));
        }

        List<Long> itemIds = items.stream()
                .map(Item::getId)
                .toList();

        List<Issue> issues = issueRepository.findByItemIdInAndResolvedFalse(itemIds);

        Map<Long, List<String>> issueTypesByItemId = issues.stream()
                .collect(Collectors.groupingBy(
                        issue -> issue.getItem().getId(),
                        Collectors.mapping(issue -> issue.getIssueType().name(), Collectors.toList())
                ));

        Page<ItemListResponseDto> page = itemPage.map(item ->
                ItemListResponseDto.from(item, issueTypesByItemId.getOrDefault(item.getId(), List.of()))
        );

        return new PageResponseDto<>(page);
    }
}
