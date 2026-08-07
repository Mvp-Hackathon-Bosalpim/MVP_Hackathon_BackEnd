package com.bosalpim.compozi_ai.domain.inbox.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.bosalpim.compozi_ai.domain.document.entity.File;
import com.bosalpim.compozi_ai.domain.document.entity.Item;
import com.bosalpim.compozi_ai.domain.document.enums.InputMethod;
import com.bosalpim.compozi_ai.domain.document.enums.ReviewStatus;
import com.bosalpim.compozi_ai.domain.document.enums.SourceType;
import com.bosalpim.compozi_ai.domain.document.repository.FileRepository;
import com.bosalpim.compozi_ai.domain.document.repository.ItemRepository;
import com.bosalpim.compozi_ai.domain.inbox.dto.response.BulkActionResponseDto;
import com.bosalpim.compozi_ai.domain.inbox.entity.Issue;
import com.bosalpim.compozi_ai.domain.inbox.enums.IssueType;
import com.bosalpim.compozi_ai.domain.inbox.repository.IssueRepository;
import jakarta.transaction.Transactional;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Transactional
class IssueServiceTest {

    @Autowired
    private IssueService issueService;
    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private FileRepository fileRepository;
    @Autowired
    private IssueRepository issueRepository;


    private Item saveItem(String docId, ReviewStatus status) {
        File file = fileRepository.save(File.createFile("test.csv", InputMethod.FILE));
        Item item = Item.builder()
                .file(file)
                .docId(docId)
                .sourceType(SourceType.CSV)
                .supplierName("가온푸드")
                .rawItemName("토마토살사S/O")
                .reviewStatus(status)
                .build();
        return itemRepository.save(item);
    }

    @Test
    void 미해결_이슈가_없으면_승인에_성공한다() {
        // given
        File file = File.createFile("test.csv", InputMethod.FILE);
        fileRepository.save(file);

        Item item = Item.builder()
                .file(file)
                .docId("DOC-001")
                .sourceType(SourceType.CSV)
                .supplierName("가온푸드")
                .rawItemName("토마토살사S/O")
                .reviewStatus(ReviewStatus.NEW)
                .build();
        itemRepository.save(item);

        // when
        Long approvedId = issueService.approve(item.getId());

        // then
        Item approved = itemRepository.findById(approvedId).orElseThrow();
        assertThat(approved.getReviewStatus()).isEqualTo(ReviewStatus.APPROVED);
    }

    @Test
    @DisplayName("단건_단일_테스트")
    void 단건_단일_테스트() throws Exception {
        //given
        File file = File.createFile("test.csv", InputMethod.FILE);
        fileRepository.save(file);

        Item item = Item.builder()
                .file(file)
                .docId("DOC-001")
                .sourceType(SourceType.CSV)
                .supplierName("가온푸드")
                .rawItemName("토마토살사S/O")
                .reviewStatus(ReviewStatus.NEW)
                .build();
        itemRepository.save(item);

        //when
        Long rejectedId = issueService.reject(item.getId(), "알 수 없는 값이 있다.");

        //then
        Item rejected = itemRepository.findById(rejectedId).orElseThrow();
        assertThat(rejected.getReviewStatus()).isEqualTo(ReviewStatus.REJECTED);
    }

    @Test
    @DisplayName("세건_모두_전체_승인")
    void 세건_모두_전체_승인() {
        // given
        Item item1 = saveItem("DOC-201", ReviewStatus.NEW);
        Item item2 = saveItem("DOC-202", ReviewStatus.NEW);
        Item item3 = saveItem("DOC-203", ReviewStatus.NEW);

        // when
        BulkActionResponseDto result = issueService.bulkApprove(
                List.of(item1.getId(), item2.getId(), item3.getId()));

        // then
        assertThat(result.getRequestedCount()).isEqualTo(3);
        assertThat(result.getSuccessCount()).isEqualTo(3);
        assertThat(result.getFailedCount()).isEqualTo(0);
        assertThat(result.getSuccessIds()).containsExactlyInAnyOrder(
                item1.getId(), item2.getId(), item3.getId());

        assertThat(itemRepository.findById(item1.getId()).orElseThrow().getReviewStatus())
                .isEqualTo(ReviewStatus.APPROVED);
    }

    @Test
    @DisplayName("세건_중_부분_승인")
    void 세건_중_부분_승인() {
        // given
        Item newItem = saveItem("DOC-204", ReviewStatus.NEW);
        Item approvedItem = saveItem("DOC-205", ReviewStatus.APPROVED);
        Long notExistId = 9999L;

        // when
        BulkActionResponseDto result = issueService.bulkApprove(
                List.of(newItem.getId(), approvedItem.getId(), notExistId));

        // then
        assertThat(result.getRequestedCount()).isEqualTo(3);
        assertThat(result.getSuccessCount()).isEqualTo(1);
        assertThat(result.getFailedCount()).isEqualTo(2);
        assertThat(result.getSuccessIds()).containsExactly(newItem.getId());

        List<Long> failedIds = result.getFailedList().stream()
                .map(BulkActionResponseDto.FailedItemDto::getId)
                .toList();
        assertThat(failedIds).containsExactlyInAnyOrder(approvedItem.getId(), notExistId);
    }

    @DisplayName("미해결_이슈_존재시_실패")
    @Test
    void 미해결_이슈_존재시_실패() {
        // given
        Item item = saveItem("DOC-206", ReviewStatus.NEEDS_REVIEW);
        issueRepository.save(Issue.builder()
                .item(item)
                .issueType(IssueType.SPEC_MISMATCH)
                .resolved(false)
                .build());
        issueRepository.save(Issue.builder()
                .item(item)
                .issueType(IssueType.UNIT_MISMATCH)
                .resolved(false)
                .build());

        // when
        BulkActionResponseDto result = issueService.bulkApprove(List.of(item.getId()));

        // then
        assertThat(result.getSuccessCount()).isEqualTo(0);
        assertThat(result.getFailedCount()).isEqualTo(1);

        BulkActionResponseDto.FailedItemDto failed = result.getFailedList().get(0);
        assertThat(failed.getId()).isEqualTo(item.getId());
        assertThat(failed.getIssueTypes()).containsExactlyInAnyOrder("SPEC_MISMATCH", "UNIT_MISMATCH");
    }
}

