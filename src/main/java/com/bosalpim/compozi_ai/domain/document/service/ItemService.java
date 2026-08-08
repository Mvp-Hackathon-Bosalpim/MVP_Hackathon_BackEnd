package com.bosalpim.compozi_ai.domain.document.service;

import com.bosalpim.compozi_ai.domain.document.component.validator.ItemDocumentDuplicateValidator;
import com.bosalpim.compozi_ai.domain.document.component.validator.ItemSpecAndUnitValidator;
import com.bosalpim.compozi_ai.domain.document.dto.request.commonFile.CreateCommonItemDocumentReqDto;
import com.bosalpim.compozi_ai.domain.document.dto.request.manualFile.CheckDuplicatedManualItemDto;
import com.bosalpim.compozi_ai.domain.document.dto.request.manualFile.CreateManualItemDocumentListReqDto;
import com.bosalpim.compozi_ai.domain.document.dto.request.manualFile.CreateManualItemDocumentReqDto;
import com.bosalpim.compozi_ai.domain.document.entity.File;
import com.bosalpim.compozi_ai.domain.document.entity.Item;
import com.bosalpim.compozi_ai.domain.document.enums.ReviewStatus;
import com.bosalpim.compozi_ai.domain.document.repository.ItemRepository;
import com.bosalpim.compozi_ai.domain.inbox.entity.DuplicatedGroup;
import com.bosalpim.compozi_ai.domain.inbox.entity.Issue;
import com.bosalpim.compozi_ai.domain.inbox.enums.IssueType;
import com.bosalpim.compozi_ai.domain.inbox.repository.DuplicatedGroupRepository;
import com.bosalpim.compozi_ai.domain.inbox.repository.IssueRepository;
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

@Service
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;
    private final DuplicatedGroupRepository duplicatedGroupRepository;
    private final IssueRepository issueRepository;
    private final ItemDocumentDuplicateValidator itemDocumentDuplicateValidator;
    private final ItemSpecAndUnitValidator itemSpecAndUnitValidator;
    private final Validator validator;

    public List<Item> createCommonItem(List<CreateCommonItemDocumentReqDto> reqDtos, File savedFile) {
        itemDocumentDuplicateValidator.markDuplicatesForCommon(reqDtos); // 중복 마킹

        return processAndSaveItems(
                reqDtos.size(),
                i -> reqDtos.get(i).getDuplicateGroupKey(),
                (i, group, issueCollector, isDuplicate) -> {
                    CreateCommonItemDocumentReqDto dto = reqDtos.get(i);

                    ReviewStatus reviewStatus = determineReviewStatus(dto.getSpec(), dto.getUnit(),
                            isDuplicate);

                    Set<ConstraintViolation<CreateCommonItemDocumentReqDto>> violations = validator.validate(dto);
                    boolean hasMissingField = !violations.isEmpty();

                    // 만약 보류 상태는 아닌데 빈칸이 있는 경우 (우선 순위 : 보류 상태 >>> 확인 필요)
                    if (reviewStatus.equals(ReviewStatus.NEW) && hasMissingField) {
                        reviewStatus = ReviewStatus.NEEDS_REVIEW;
                    }

                    Item item = Item.CreateCommonItem(dto, savedFile, group, reviewStatus);

                    // 규격 및 단위 이슈 검사 후 수집
                    collectIssuesIfNeeded(item, dto.getSpec(), dto.getUnit(), issueCollector, hasMissingField);

                    return item;
                }
        );
    }

    public List<Item> createManualItem(CreateManualItemDocumentListReqDto reqDtos, List<File> savedFiles) {
        List<CreateManualItemDocumentReqDto> itemDtos = reqDtos.getItems();
        List<CheckDuplicatedManualItemDto> checkedDtos = itemDocumentDuplicateValidator.markDuplicatesForManual(
                itemDtos);

        return processAndSaveItems(
                itemDtos.size(),
                i -> checkedDtos.get(i).getDuplicateGroupKey(),
                (i, group, issueCollector, isDuplicate) -> {
                    CreateManualItemDocumentReqDto itemDto = itemDtos.get(i);
                    CheckDuplicatedManualItemDto checkedDto = checkedDtos.get(i);

                    ReviewStatus reviewStatus = determineReviewStatus(itemDto.getSpec(), itemDto.getUnit(),
                            isDuplicate);

                    Set<ConstraintViolation<CreateManualItemDocumentReqDto>> violations = validator.validate(
                            itemDto);
                    boolean hasMissingField = !violations.isEmpty();

                    // 만약 보류 상태는 아닌데 빈칸이 있는 경우 (우선 순위 : 보류 상태 >>> 확인 필요)
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

                    // 규격 및 단위 이슈 검사 후 수집
                    collectIssuesIfNeeded(item, itemDto.getSpec(), itemDto.getUnit(), issueCollector, hasMissingField);

                    return item;
                }
        );
    }

    private ReviewStatus determineReviewStatus(String spec, String unit, boolean isDuplicate) {
        boolean hasSpecOrUnitIssue = itemSpecAndUnitValidator.isSpecMismatch(spec)
                || itemSpecAndUnitValidator.isUnitMismatch(unit);

        return (hasSpecOrUnitIssue || isDuplicate) ? ReviewStatus.ON_HOLD : ReviewStatus.NEW;
    }

    // --- [ 이슈 수집 공통 헬퍼 메서드 ] ---
    private void collectIssuesIfNeeded(Item item, String spec, String unit, Consumer<Issue> issueCollector,
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


    // --- [ 저장 통합 공통 메서드 ] ---
    private List<Item> processAndSaveItems(
            int size,
            Function<Integer, String> keyExtractor,
            QuadFunction<Integer, DuplicatedGroup, Consumer<Issue>, Boolean, Item> itemMapper
    ) {
        Map<String, DuplicatedGroup> groupMap = new HashMap<>();
        Set<String> seenKeys = new HashSet<>(); // 첫 등장 키

        List<Item> items = new ArrayList<>();
        List<Issue> issues = new ArrayList<>();

        // 1. 메모리 상에서 Item 생성 및 Issue 수집
        for (int i = 0; i < size; i++) {
            String duplicateKey = keyExtractor.apply(i);
            DuplicatedGroup group = null;

            if (duplicateKey != null) {
                group = groupMap.computeIfAbsent(duplicateKey, key -> DuplicatedGroup.create());
            }

            boolean isDuplicate = duplicateKey != null && seenKeys.contains(duplicateKey);

            // issues::add 를 넘겨주어 람다에서 직접 list에 저장 가능하도록 처리
            Item item = itemMapper.apply(i, group, issues::add, isDuplicate);
            items.add(item);

            if (duplicateKey != null) {
                if (seenKeys.contains(duplicateKey)) {
                    issues.add(Issue.create(IssueType.DUPLICATE_SUSPECTED, "중복 의심", false, item));
                } else {
                    seenKeys.add(duplicateKey); // 첫 번째 건은 키만 등록하고 이슈 생성을 스킵함
                }
            }
        }

        // 2. DuplicatedGroup 저장 (PK 발급)
        if (!groupMap.isEmpty()) {
            duplicatedGroupRepository.saveAll(groupMap.values());
        }

        // 3. Item 일괄 저장 (DB에 영속화 되며 Item PK 채워짐)
        List<Item> savedItems = itemRepository.saveAll(items);

        // 4. 수집된 Issue가 있으면 저장 (Item PK를 안전하게 FK로 참조)
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
