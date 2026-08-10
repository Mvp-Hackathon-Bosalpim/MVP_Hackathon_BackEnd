package com.bosalpim.compozi_ai.domain.export.repository;

import com.bosalpim.compozi_ai.domain.export.entity.ExportHistory;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExportHistoryRepository extends JpaRepository<ExportHistory, Long> {
    Page<ExportHistory> findAllByOrderByExportedAtDesc(Pageable pageable);

    List<ExportHistory> findTop2ByOrderByExportedAtDesc();
}
