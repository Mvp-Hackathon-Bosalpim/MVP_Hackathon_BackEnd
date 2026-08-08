package com.bosalpim.compozi_ai.domain.inbox.entity;

import com.bosalpim.compozi_ai.domain.document.entity.Item;
import com.bosalpim.compozi_ai.domain.inbox.enums.IssueType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "issues")
public class Issue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private IssueType issueType;

    private String detail;

    private Boolean resolved;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id")
    private Item item;

    @Builder
    public Issue(IssueType issueType, String detail, Boolean resolved, Item item) {
        this.issueType = issueType;
        this.detail = detail;
        this.resolved = resolved;
        this.item = item;
    }

    public static Issue create(IssueType issueType, String detail, Boolean resolved, Item item) {
        return Issue.builder()
                .issueType(issueType)
                .detail(detail)
                .resolved(resolved)
                .item(item)
                .build();
    }
}
