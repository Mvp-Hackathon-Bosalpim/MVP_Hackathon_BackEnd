package com.bosalpim.compozi_ai.domain.itemDoc.entity;

import com.bosalpim.compozi_ai.domain.inbox.entity.DuplicatedGroup;
import com.bosalpim.compozi_ai.domain.itemDoc.enums.ReviewStatus;
import com.bosalpim.compozi_ai.domain.itemDoc.enums.SourceType;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "items")
public class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String docId;

    private String rowNo; // manual 의 경우 null 가능

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SourceType sourceType;

    private String supplierName;

    private String rawItemName;

    private String normalizedItemName;

    private String spec;

    private String unit;

    private Integer priceBefore;

    private Integer priceAfter;

    private LocalDate effectiveDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReviewStatus reviewStatus;

    private LocalDateTime deletedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id")
    private Document document;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "duplicated_group_id")
    private DuplicatedGroup duplicatedGroup;


}
