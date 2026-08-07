package com.bosalpim.compozi_ai.domain.inbox.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.bosalpim.compozi_ai.domain.document.entity.File;
import com.bosalpim.compozi_ai.domain.document.entity.Item;
import com.bosalpim.compozi_ai.domain.document.enums.InputMethod;
import com.bosalpim.compozi_ai.domain.document.enums.ReviewStatus;
import com.bosalpim.compozi_ai.domain.document.enums.SourceType;
import com.bosalpim.compozi_ai.domain.document.repository.FileRepository;
import com.bosalpim.compozi_ai.domain.document.repository.ItemRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class IssueServiceTest {

    @Autowired
    private IssueService issueService;
    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private FileRepository fileRepository;

    @Test
    void 미해결_이슈가_없으면_승인에_성공한다() {
        // given: 더미 File, Item 하나 저장
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
}