package com.bosalpim.compozi_ai.domain.document.service;

import com.bosalpim.compozi_ai.domain.document.component.validator.ItemDocumentDuplicateValidator;
import com.bosalpim.compozi_ai.domain.document.component.validator.ItemDocumentDuplicateValidator.DuplicateValidationResult;
import com.bosalpim.compozi_ai.domain.document.component.validator.ItemSpecAndUnitValidator;
import com.bosalpim.compozi_ai.domain.document.dto.request.commonFile.CreateCommonItemDocumentReqDto;
import com.bosalpim.compozi_ai.domain.document.dto.request.manualFile.CheckDuplicatedManualItemDto;
import com.bosalpim.compozi_ai.domain.document.dto.request.manualFile.CreateManualItemDocumentListReqDto;
import com.bosalpim.compozi_ai.domain.document.dto.request.manualFile.CreateManualItemDocumentReqDto;
import com.bosalpim.compozi_ai.domain.document.entity.File;
import com.bosalpim.compozi_ai.domain.document.entity.Item;
import com.bosalpim.compozi_ai.domain.document.enums.ReviewStatus;
import com.bosalpim.compozi_ai.domain.document.repository.item.ItemRepository;
import com.bosalpim.compozi_ai.domain.inbox.entity.DuplicatedGroup;
import com.bosalpim.compozi_ai.domain.inbox.entity.Issue;
import com.bosalpim.compozi_ai.domain.inbox.enums.IssueType;
import com.bosalpim.compozi_ai.domain.inbox.repository.DuplicatedGroupRepository;
import com.bosalpim.compozi_ai.domain.inbox.repository.issue.IssueRepository;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;
    private final DuplicatedGroupRepository duplicatedGroupRepository;
    private final IssueRepository issueRepository;
    private final ItemDocumentDuplicateValidator itemDocumentDuplicateValidator;
    private final ItemSpecAndUnitValidator itemSpecAndUnitValidator;
    private final Validator validator;

    @Transactional
    public List<Item> createCommonItem(List<CreateCommonItemDocumentReqDto> reqDtos, File savedFile) {
        DuplicateValidationResult validationResult = itemDocumentDuplicateValidator.markDuplicatesForCommon(reqDtos,
                itemRepository.findAllByDeletedAtIsNullOrderByIdAsc());

        return processAndSaveItemsWithDbCheck(
                reqDtos.size(),
                i -> reqDtos.get(i).getDuplicateGroupKey(),
                validationResult.existingDbMap(),
                (i, group, issueCollector, isDuplicate) -> {
                    CreateCommonItemDocumentReqDto dto = reqDtos.get(i);

                    ReviewStatus reviewStatus = determineReviewStatus(dto.getSpec(), dto.getUnit(), isDuplicate);

                    Set<ConstraintViolation<CreateCommonItemDocumentReqDto>> violations = validator.validate(dto);
                    boolean hasMissingField = !violations.isEmpty();

                    if (dto.isHasParseError() || reviewStatus.equals(ReviewStatus.NEW) && hasMissingField) {
                        reviewStatus = ReviewStatus.NEEDS_REVIEW;
                    }

                    Item item = Item.CreateCommonItem(dto, savedFile, group, reviewStatus);

                    collectIssuesIfNeeded(item, dto.getSpec(), dto.getUnit(), issueCollector, hasMissingField);

                    return item;
                }
        );
    }

    @Transactional
    public List<Item> createManualItem(CreateManualItemDocumentListReqDto reqDtos, List<File> savedFiles) {
        List<CreateManualItemDocumentReqDto> itemDtos = reqDtos.getItems();
        DuplicateValidationResult validationResult = itemDocumentDuplicateValidator.markDuplicatesForManual(itemDtos,
                itemRepository.findAllByDeletedAtIsNullOrderByIdAsc());

        @SuppressWarnings("unchecked")
        List<CheckDuplicatedManualItemDto> checkedDtos = (List<CheckDuplicatedManualItemDto>) validationResult.firstSeenInRequestMap();

        return processAndSaveItemsWithDbCheck(
                itemDtos.size(),
                i -> checkedDtos.get(i).getDuplicateGroupKey(),
                validationResult.existingDbMap(),
                (i, group, issueCollector, isDuplicate) -> {
                    CreateManualItemDocumentReqDto itemDto = itemDtos.get(i);
                    CheckDuplicatedManualItemDto checkedDto = checkedDtos.get(i);

                    ReviewStatus reviewStatus = determineReviewStatus(itemDto.getSpec(), itemDto.getUnit(),
                            isDuplicate);

                    Set<ConstraintViolation<CreateManualItemDocumentReqDto>> violations = validator.validate(itemDto);
                    boolean hasMissingField = !violations.isEmpty();

                    if (reviewStatus.equals(ReviewStatus.NEW) && hasMissingField) {
                        reviewStatus = ReviewStatus.NEEDS_REVIEW;
                    }

                    Item item = Item.CreateManualItem(
                            itemDto,
                            savedFiles.get(i),
                            checkedDto.getNormalizedItemName(),
                            group,
                            reviewStatus
                    );

                    collectIssuesIfNeeded(item, itemDto.getSpec(), itemDto.getUnit(), issueCollector, hasMissingField);

                    return item;
                }
        );
    }

    public ReviewStatus determineReviewStatus(String spec, String unit, boolean isDuplicate) {
        boolean hasSpecOrUnitIssue = itemSpecAndUnitValidator.isSpecMismatch(spec)
                || itemSpecAndUnitValidator.isUnitMismatch(unit);

        return (hasSpecOrUnitIssue || isDuplicate) ? ReviewStatus.ON_HOLD : ReviewStatus.NEW;
    }

    public void collectIssuesIfNeeded(Item item, String spec, String unit, Consumer<Issue> issueCollector,
                                      boolean hasMissingField) {
        if (itemSpecAndUnitValidator.isSpecMismatch(spec)) {
            issueCollector.accept(Issue.create(IssueType.SPEC_MISMATCH, "규격 불일치", false, item));
        }
        if (itemSpecAndUnitValidator.isUnitMismatch(unit)) {
            issueCollector.accept(Issue.create(IssueType.UNIT_MISMATCH, "단위 불일치", false, item));
        }
        if (hasMissingField) {
            issueCollector.accept(Issue.create(IssueType.MISSING_REQUIRED, "필수값 누락", false, item));
        }
    }

    // --- [ 기존 그룹 병합 로직 수정 반영 메서드 ] ---
    private List<Item> processAndSaveItemsWithDbCheck(
            int size,
            Function<Integer, String> keyExtractor,
            Map<String, Item> existingDbMap,
            QuadFunction<Integer, DuplicatedGroup, Consumer<Issue>, Boolean, Item> itemMapper
    ) {
        Map<String, DuplicatedGroup> groupMap = new HashMap<>();
        Set<String> seenKeys = new HashSet<>();

        List<Item> itemsToSave = new ArrayList<>();
        List<Item> existingItemsToUpdate = new ArrayList<>();
        List<Issue> issues = new ArrayList<>();

        for (int i = 0; i < size; i++) {
            String duplicateKey = keyExtractor.apply(i);
            DuplicatedGroup group = null;

            if (duplicateKey != null) {

                // 기존 DB 항목이 이미 DuplicatedGroup을 가지고 있으면 해당 그룹을 재활용
                group = groupMap.computeIfAbsent(duplicateKey, key -> {
                    Item originalDbItem = existingDbMap.get(key);

                    if (originalDbItem != null) {
                        // 1. DB 항목에 이미 존재하는 중복 그룹이 있는 경우 -> 그 그룹 재사용
                        if (originalDbItem.getDuplicatedGroup() != null) {
                            return originalDbItem.getDuplicatedGroup();
                        }

                        // 2. DB 항목은 있지만 아직 중복 그룹이 없는 경우 -> 새 그룹 생성 및 DB 항목 업데이트
                        DuplicatedGroup newGroup = DuplicatedGroup.create();
                        originalDbItem.updateDuplicatedGroup(newGroup);
                        existingItemsToUpdate.add(originalDbItem);
                        return newGroup;
                    }

                    // 3. DB 항목도 없는 순수 요청 내 중복 -> 새 그룹 생성
                    return DuplicatedGroup.create();
                });
            }

            boolean isDuplicate = (duplicateKey != null) &&
                    (existingDbMap.containsKey(duplicateKey) || seenKeys.contains(duplicateKey));

            Item item = itemMapper.apply(i, group, issues::add, isDuplicate);
            itemsToSave.add(item);

            if (duplicateKey != null) {
                if (isDuplicate) {
                    issues.add(Issue.create(IssueType.DUPLICATE_SUSPECTED, "중복 의심", false, item));
                } else {
                    seenKeys.add(duplicateKey);
                }
            }
        }

        // 1. 신규 생성된 DuplicatedGroup 중 영속화되지 않은(id가 null인) 그룹들만 필터링하여 저장
        List<DuplicatedGroup> newGroupsToSave = groupMap.values().stream()
                .filter(g -> g.getId() == null)
                .toList();

        if (!newGroupsToSave.isEmpty()) {
            duplicatedGroupRepository.saveAll(newGroupsToSave);
        }

        // 2. 그룹이 새로 할당된 기존 DB 데이터 업데이트
        if (!existingItemsToUpdate.isEmpty()) {
            itemRepository.saveAll(existingItemsToUpdate);
        }

        // 3. 신규 Item 일괄 저장
        List<Item> savedItems = itemRepository.saveAll(itemsToSave);

        // 4. 중복 이슈 저장
        if (!issues.isEmpty()) {
            issueRepository.saveAll(issues);
        }

        return savedItems;
    }

    @FunctionalInterface
    public interface QuadFunction<T, U, V, W, R> {
        R apply(T t, U u, V v, W w);
    }
}
