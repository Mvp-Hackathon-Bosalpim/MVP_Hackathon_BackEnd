package com.bosalpim.compozi_ai.domain.inbox.service;

import com.bosalpim.compozi_ai.domain.document.entity.Item;
import com.bosalpim.compozi_ai.domain.document.enums.ReviewStatus;
import com.bosalpim.compozi_ai.domain.document.repository.ItemRepository;
import com.bosalpim.compozi_ai.domain.inbox.entity.ChangeLog;
import com.bosalpim.compozi_ai.domain.inbox.enums.Action;
import com.bosalpim.compozi_ai.domain.inbox.repository.ChangeLogRepository;
import com.bosalpim.compozi_ai.domain.inbox.repository.IssueRepository;
import com.bosalpim.compozi_ai.general.enums.BadStatusCode;
import com.bosalpim.compozi_ai.general.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class IssueService {
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

}
