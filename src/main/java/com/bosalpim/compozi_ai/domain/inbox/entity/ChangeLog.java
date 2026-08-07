package com.bosalpim.compozi_ai.domain.inbox.entity;

import com.bosalpim.compozi_ai.domain.document.entity.Item;
import com.bosalpim.compozi_ai.domain.inbox.enums.Action;
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
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "change_logs")
public class ChangeLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Action action;

    private String fieldName;

    private String fromValue;

    private String toValue;

    private String memo;

    private LocalDateTime at;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id")
    private Item item;

    @Builder
    public ChangeLog(Action action, String fieldName, String fromValue, String toValue, String memo, LocalDateTime at,
                     Item item) {
        this.action = action;
        this.fieldName = fieldName;
        this.fromValue = fromValue;
        this.toValue = toValue;
        this.memo = memo;
        this.at = at;
        this.item = item;
    }

    public static ChangeLog of(Item item, Action action) {
        return ChangeLog.builder()
                .item(item)
                .action(action)
                .at(LocalDateTime.now())
                .build();
    }
}
