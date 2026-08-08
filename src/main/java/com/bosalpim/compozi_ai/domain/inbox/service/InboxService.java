package com.bosalpim.compozi_ai.domain.inbox.service;

import com.bosalpim.compozi_ai.domain.document.entity.Item;
import com.bosalpim.compozi_ai.domain.document.enums.ReviewStatus;
import com.bosalpim.compozi_ai.domain.document.repository.ItemRepository;
import com.bosalpim.compozi_ai.domain.inbox.dto.response.BulkActionResponseDto;
import com.bosalpim.compozi_ai.domain.inbox.dto.response.ItemListResponseDto;
import com.bosalpim.compozi_ai.domain.inbox.entity.ChangeLog;
import com.bosalpim.compozi_ai.domain.inbox.entity.Issue;
import com.bosalpim.compozi_ai.domain.inbox.enums.Action;
import com.bosalpim.compozi_ai.domain.inbox.repository.ChangeLogRepository;
import com.bosalpim.compozi_ai.domain.inbox.repository.IssueRepository;
import com.bosalpim.compozi_ai.general.enums.BadStatusCode;
import com.bosalpim.compozi_ai.general.exception.CustomException;
import com.bosalpim.compozi_ai.general.response.PageResponseDto;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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

    @Transactional
    public Long approve(Long id) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new CustomException(BadStatusCode.ITEM_NOT_FOUND));

        if (item.getReviewStatus() == ReviewStatus.APPROVED) {
            throw new CustomException(BadStatusCode.ITEM_ALREADY_APPROVED);
        }

        boolean isUnresolvedIssue = issueRepository.existsByItemIdAndResolvedFalse(item.getId());

        if (isUnresolvedIssue) {
            throw new CustomException(BadStatusCode.UNRESOLVED_ISSUE_EXISTS);
        }

        item.approve();
        changeLogRepository.save(ChangeLog.of(item, Action.APPROVE));

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
    public BulkActionResponseDto bulkApprove(List<Long> ids) {

        // Item들을 한 번의 쿼리로 다 가져옴 (쿼리1)
        List<Item> items = itemRepository.findAllById(ids);
        Map<Long, Item> itemMap = items.stream()
                .collect(Collectors.toMap(Item::getId, item -> item));

        List<Long> itemIds = new ArrayList<>(itemMap.keySet());

        // 쿼리 2
        List<Issue> unresolvedIssues = issueRepository.findByItemIdInAndResolvedFalse(itemIds);

        Map<Long, List<String>> issueTypesByItemId = unresolvedIssues.stream()
                .collect(Collectors.groupingBy(
                        issue -> issue.getItem().getId(),
                        Collectors.mapping(issue -> issue.getIssueType().name(), Collectors.toList())
                ));

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
            if (issueTypesByItemId.containsKey(id)) {
                failedList.add(new BulkActionResponseDto.FailedItemDto(
                        id, BadStatusCode.UNRESOLVED_ISSUE_EXISTS, issueTypesByItemId.get(id)));
                continue;
            }

            item.approve();
            logsToSave.add(ChangeLog.of(item, Action.APPROVE));
            successIds.add(id);
        }

        if (successIds.isEmpty()) {
            throw new CustomException(BadStatusCode.ALL_ITEMS_FAILED);
        }
        //쿼리3
        changeLogRepository.saveAll(logsToSave);
        // 쿼리4
        return new BulkActionResponseDto(ids.size(), successIds.size(), failedList.size(), successIds, failedList);
    }

    @Transactional
    public BulkActionResponseDto bulkReject(List<Long> ids) {

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
            logsToSave.add(ChangeLog.of(item, Action.REJECT));
            successIds.add(id);
        }

        if (successIds.isEmpty()) {
            throw new CustomException(BadStatusCode.ALL_ITEMS_FAILED);
        }

        changeLogRepository.saveAll(logsToSave);

        return new BulkActionResponseDto(ids.size(), successIds.size(), failedList.size(), successIds, failedList);
    }

    @Transactional
    public BulkActionResponseDto bulkReReview(List<Long> ids) {

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
            logsToSave.add(ChangeLog.of(item, Action.RE_REVIEW));
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
}
