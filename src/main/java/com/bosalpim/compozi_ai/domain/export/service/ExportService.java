package com.bosalpim.compozi_ai.domain.export.service;

import com.bosalpim.compozi_ai.domain.document.entity.Item;
import com.bosalpim.compozi_ai.domain.document.enums.ReviewStatus;
import com.bosalpim.compozi_ai.domain.document.repository.item.ItemRepository;
import com.bosalpim.compozi_ai.domain.export.dto.ExportData;
import com.bosalpim.compozi_ai.domain.export.dto.request.ExportRequestDto;
import com.bosalpim.compozi_ai.domain.export.dto.response.ExportHistoryResponseDto;
import com.bosalpim.compozi_ai.domain.export.dto.response.ExportItemCsvResponseDto;
import com.bosalpim.compozi_ai.domain.export.dto.response.ExportItemJsonResponseDto;
import com.bosalpim.compozi_ai.domain.export.entity.ExportHistory;
import com.bosalpim.compozi_ai.domain.export.enums.ExportFormat;
import com.bosalpim.compozi_ai.domain.export.repository.ExportHistoryRepository;
import com.bosalpim.compozi_ai.domain.export.s3.service.S3Service;
import com.bosalpim.compozi_ai.domain.inbox.entity.ChangeLog;
import com.bosalpim.compozi_ai.domain.inbox.entity.Issue;
import com.bosalpim.compozi_ai.domain.inbox.enums.Action;
import com.bosalpim.compozi_ai.domain.inbox.repository.change_log.ChangeLogRepository;
import com.bosalpim.compozi_ai.domain.inbox.repository.issue.IssueRepository;
import com.bosalpim.compozi_ai.general.enums.BadStatusCode;
import com.bosalpim.compozi_ai.general.exception.CustomException;
import com.bosalpim.compozi_ai.general.response.PageResponseDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.CSVWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExportService {

    private static final DateTimeFormatter FILE_TIMESTAMP = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    private final ItemRepository itemRepository;
    private final IssueRepository issueRepository;
    private final ChangeLogRepository changeLogRepository;
    private final ExportHistoryRepository exportHistoryRepository;
    private final S3Service s3Service;
    private final ObjectMapper objectMapper;

    public ExportHistory export(ExportRequestDto request) {
        validateRequest(request);

        return switch (request.getFormat()) {
            case JSON -> exportJson(request.getFileName());
            case CSV -> exportCsv(request.getFileName());
        };
    }

    public String getDownloadUrl(Long exportHistoryId) {
        ExportHistory history = exportHistoryRepository.findById(exportHistoryId)
                .orElseThrow(() -> new CustomException(BadStatusCode.EXPORT_HISTORY_NOT_FOUND));

        if (history.getS3Key() == null) {
            throw new CustomException(BadStatusCode.EXPORT_S3_KEY_NOT_FOUND);
        }

        return s3Service.generatePresignedUrl(history.getS3Key(), Duration.ofDays(7));
    }

    private void validateRequest(ExportRequestDto request) {
        if (request.getFileName() == null || request.getFileName().isBlank()) {
            throw new CustomException(BadStatusCode.EXPORT_FILE_NAME_REQUIRED);
        }
    }

    private ExportHistory exportJson(String rawFileName) {
        ExportData data = loadExportData();
        List<ExportItemJsonResponseDto> exportItems = data.getItems().stream()
                .map(item -> toJsonResponseDto(item, data))
                .toList();

        String fileName = rawFileName + ".json";
        String key = "exports/json/" + LocalDateTime.now().format(FILE_TIMESTAMP) + "_" + fileName;

        try {
            byte[] bytes = objectMapper.writeValueAsBytes(exportItems);
            s3Service.upload(key, bytes).join();

            ExportHistory history = ExportHistory.completed(fileName, key, ExportFormat.JSON, exportItems.size());
            return exportHistoryRepository.save(history);

        } catch (Exception e) {
            log.error("JSON export 실패", e);
            exportHistoryRepository.save(ExportHistory.failed(fileName, ExportFormat.JSON));
            throw new CustomException(BadStatusCode.EXPORT_UPLOAD_FAILED);
        }
    }

    private ExportHistory exportCsv(String rawFileName) {
        ExportData data = loadExportData();
        List<ExportItemCsvResponseDto> exportItems = data.getItems().stream()
                .map(item -> toCsvResponseDto(item, data))
                .toList();

        String fileName = rawFileName + ".csv";
        String key = "exports/csv/" + LocalDateTime.now().format(FILE_TIMESTAMP) + "_" + fileName;

        try {
            byte[] bytes = writeCsv(exportItems);
            s3Service.upload(key, bytes).join();

            ExportHistory history = ExportHistory.completed(fileName, key, ExportFormat.CSV, exportItems.size());
            return exportHistoryRepository.save(history);

        } catch (Exception e) {
            log.error("CSV export 실패", e);
            exportHistoryRepository.save(ExportHistory.failed(fileName, ExportFormat.CSV));
            throw new CustomException(BadStatusCode.EXPORT_UPLOAD_FAILED);
        }
    }

    // == JSON 변환 == //

    private ExportItemJsonResponseDto toJsonResponseDto(Item item, ExportData data) {
        List<ChangeLog> logs = data.changeLogsOf(item.getId());

        return new ExportItemJsonResponseDto(
                item.getDocId(),
                item.getSourceType().name(),
                item.getSupplierName(),
                item.getRawItemName(),
                item.getNormalizedItemName(),
                item.getSpec(),
                item.getUnit(),
                item.getPriceBefore(),
                item.getPriceAfter(),
                item.getEffectiveDate(),
                item.getReviewStatus().name(),
                extractExceptionFlags(item.getId(), data),
                extractSourceRef(item),
                extractReviewedAt(logs),
                extractReviewMemo(logs),
                extractChangeLogs(logs)
        );
    }

    private ExportItemJsonResponseDto.SourceRef extractSourceRef(Item item) {
        return new ExportItemJsonResponseDto.SourceRef(
                item.getFile().getInputMethod().name(),
                item.getFile().getFileName(),
                item.getRowNo()
        );
    }

    private List<ExportItemJsonResponseDto.ChangeLogDto> extractChangeLogs(List<ChangeLog> logs) {
        return logs.stream()
                .filter(log -> log.getAction() == Action.EDIT)
                .map(log -> new ExportItemJsonResponseDto.ChangeLogDto(
                        log.getAt(),
                        log.getFieldName(),
                        log.getFromValue(),
                        log.getToValue(),
                        log.getAction().name()
                ))
                .toList();
    }

    // == CSV 변환 == //

    private ExportItemCsvResponseDto toCsvResponseDto(Item item, ExportData data) {
        List<ChangeLog> logs = data.changeLogsOf(item.getId());
        String exceptionFlags = String.join(";", extractExceptionFlags(item.getId(), data));

        List<ChangeLog> edits = logs.stream()
                .filter(log -> log.getAction() == Action.EDIT)
                .sorted(Comparator.comparing(ChangeLog::getAt))
                .toList();

        return new ExportItemCsvResponseDto(
                item.getDocId(),
                item.getSourceType().name(),
                item.getSupplierName(),
                item.getRawItemName(),
                item.getNormalizedItemName(),
                item.getSpec(),
                item.getUnit(),
                item.getPriceBefore(),
                item.getPriceAfter(),
                item.getEffectiveDate(),
                item.getReviewStatus().name(),
                exceptionFlags,
                item.getFile().getInputMethod().name(),
                item.getFile().getFileName(),
                item.getRowNo(),
                extractReviewedAt(logs),
                extractReviewMemo(logs),
                joinEditField(edits, log -> log.getAt().toString()),
                joinEditField(edits, ChangeLog::getFieldName),
                joinEditField(edits, ChangeLog::getFromValue),
                joinEditField(edits, ChangeLog::getToValue),
                joinEditField(edits, log -> log.getAction().name())
        );
    }

    private String joinEditField(List<ChangeLog> edits, java.util.function.Function<ChangeLog, String> extractor) {
        return edits.stream()
                .map(extractor)
                .collect(java.util.stream.Collectors.joining(";"));
    }

    private byte[] writeCsv(List<ExportItemCsvResponseDto> items) throws IOException {
        StringWriter stringWriter = new StringWriter();

        try (CSVWriter csvWriter = new CSVWriter(stringWriter)) {
            String[] header = {
                    "doc_id", "source_type", "supplier_name", "raw_item_name", "normalized_item_name",
                    "spec", "unit", "price_before", "price_after", "effective_date", "review_status",
                    "exception_flags", "source_input_method", "source_file_name", "source_row_no",
                    "reviewed_at", "review_memo",
                    "change_log_at", "change_log_field", "change_log_from", "change_log_to", "change_log_action"
            };
            csvWriter.writeNext(header);

            for (ExportItemCsvResponseDto dto : items) {
                String[] row = {
                        dto.getDocId(),
                        dto.getSourceType(),
                        dto.getSupplierName(),
                        dto.getRawItemName(),
                        dto.getNormalizedItemName(),
                        dto.getSpec(),
                        dto.getUnit(),
                        String.valueOf(dto.getPriceBefore()),
                        String.valueOf(dto.getPriceAfter()),
                        dto.getEffectiveDate() != null ? dto.getEffectiveDate().toString() : "",
                        dto.getReviewStatus(),
                        dto.getExceptionFlags(),
                        dto.getSourceInputMethod(),
                        dto.getSourceFileName(),
                        dto.getSourceRowNo() != null ? String.valueOf(dto.getSourceRowNo()) : "",
                        dto.getReviewedAt() != null ? dto.getReviewedAt().toString() : "",
                        dto.getReviewMemo(),
                        dto.getChangeLogAt(),
                        dto.getChangeLogField(),
                        dto.getChangeLogFrom(),
                        dto.getChangeLogTo(),
                        dto.getChangeLogAction()
                };
                csvWriter.writeNext(row);
            }
        }

        byte[] bom = {(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};
        byte[] csvBytes = stringWriter.toString().getBytes(StandardCharsets.UTF_8);

        byte[] result = new byte[bom.length + csvBytes.length];
        System.arraycopy(bom, 0, result, 0, bom.length);
        System.arraycopy(csvBytes, 0, result, bom.length, csvBytes.length);

        return result;
    }

    // == 공통 추출 (JSON/CSV 공유) == //

    private ExportData loadExportData() {
        List<Item> items = itemRepository.findAllByReviewStatusWithFile(ReviewStatus.APPROVED);

        if (items.isEmpty()) {
            throw new CustomException(BadStatusCode.EXPORT_NO_APPROVED_ITEMS);
        }

        List<Long> itemIds = items.stream().map(Item::getId).toList();

        Map<Long, List<Issue>> issuesByItemId = issueRepository.findByItemIdInAndResolvedFalse(itemIds).stream()
                .collect(Collectors.groupingBy(issue -> issue.getItem().getId()));

        Map<Long, List<ChangeLog>> changeLogsByItemId = changeLogRepository.findAllByItemIdIn(itemIds).stream()
                .collect(Collectors.groupingBy(log -> log.getItem().getId()));

        return new ExportData(items, issuesByItemId, changeLogsByItemId);
    }

    private List<String> extractExceptionFlags(Long itemId, ExportData data) {
        return data.issuesOf(itemId).stream()
                .map(issue -> issue.getIssueType().name())
                .toList();
    }

    private LocalDateTime extractReviewedAt(List<ChangeLog> logs) {
        return logs.stream()
                .filter(log -> log.getAction() == Action.APPROVE)
                .max(Comparator.comparing(ChangeLog::getAt))
                .map(ChangeLog::getAt)
                .orElse(null);
    }

    private String extractReviewMemo(List<ChangeLog> logs) {
        return logs.stream()
                .filter(log -> log.getAction() == Action.APPROVE || log.getAction() == Action.REJECT)
                .filter(log -> log.getMemo() != null)
                .max(Comparator.comparing(ChangeLog::getAt))
                .map(ChangeLog::getMemo)
                .orElse("");
    }

    // History 내역 조회 //
    public PageResponseDto<ExportHistoryResponseDto> getHistories(int page, int size) {

        Pageable pageable = PageRequest.of(page, size);

        Page<ExportHistory> histories =
                exportHistoryRepository.findAllByOrderByExportedAtDesc(pageable);

        Page<ExportHistoryResponseDto> response =
                histories.map(ExportHistoryResponseDto::from);

        return new PageResponseDto<>(response);
    }
}