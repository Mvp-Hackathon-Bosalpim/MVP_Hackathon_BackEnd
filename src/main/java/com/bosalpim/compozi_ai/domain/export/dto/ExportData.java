package com.bosalpim.compozi_ai.domain.export.dto;

import com.bosalpim.compozi_ai.domain.document.entity.Item;
import com.bosalpim.compozi_ai.domain.inbox.entity.ChangeLog;
import com.bosalpim.compozi_ai.domain.inbox.entity.Issue;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ExportData {
    private final List<Item> items;
    private final Map<Long, List<Issue>> issuesByItemId;
    private final Map<Long, List<ChangeLog>> changeLogsByItemId;

    public List<Issue> issuesOf(Long itemId) {
        return issuesByItemId.getOrDefault(itemId, List.of());
    }

    public List<ChangeLog> changeLogsOf(Long itemId) {
        return changeLogsByItemId.getOrDefault(itemId, List.of());
    }
}