package com.bosalpim.compozi_ai.config;

import com.bosalpim.compozi_ai.domain.document.entity.File;
import com.bosalpim.compozi_ai.domain.document.entity.Item;
import com.bosalpim.compozi_ai.domain.document.enums.InputMethod;
import com.bosalpim.compozi_ai.domain.document.enums.ReviewStatus;
import com.bosalpim.compozi_ai.domain.document.enums.SourceType;
import com.bosalpim.compozi_ai.domain.document.repository.FileRepository;
import com.bosalpim.compozi_ai.domain.document.repository.ItemRepository;
import jakarta.annotation.PostConstruct;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@RequiredArgsConstructor
@Profile("init")
@Log4j2
public class SampleDataConfig {

    private final FileRepository fileRepository;
    private final ItemRepository itemRepository;

    @PostConstruct
    public void init() {
        File file = File.createFile("42_해커톤_업로드용_증빙20건_2026-08-04.csv", InputMethod.FILE);
        fileRepository.save(file);

        itemRepository.save(
                item(file, "DOC-001", 2L, SourceType.PDF, "가온푸드", "토마토살사S/O", "토마토 살사 소스", "4kg/PK", "PK", 32000L,
                        33600L, "2026-08-01"));
        itemRepository.save(
                item(file, "DOC-002", 3L, SourceType.XLSX, "새봄식품", "허브염지닭정육", "허브 염지 닭정육", "2kg×6PK/BOX", "BOX", 84000L,
                        88200L, "2026-08-01"));
        itemRepository.save(
                item(file, "DOC-003", 4L, SourceType.IMAGE, "가온푸드", "밀또띠아10인치", "밀 또띠아 10인치", "12매×10PK/BOX", "BOX",
                        36000L, 37800L, "2026-08-05"));
        itemRepository.save(
                item(file, "DOC-004", 5L, SourceType.MANUAL, "한결유통", "로메인쉬레드", "로메인 쉬레드", "1kg/PK", "PK", 8500L, 9200L,
                        "2026-08-03"));
        itemRepository.save(
                item(file, "DOC-005", 6L, SourceType.PDF, "한결유통", "아보카도30입", "아보카도", "30EA/BOX", "BOX", 65000L, 69000L,
                        "2026-08-03"));
        itemRepository.save(
                item(file, "DOC-006", 7L, SourceType.XLSX, "새봄식품", "슈레드치즈2.5K", "슈레드 치즈", "2.5kg×4PK/BOX", "BOX",
                        98000L, 103000L, "2026-08-10"));
        itemRepository.save(
                item(file, "DOC-007", 8L, SourceType.PDF, "새봄식품", "사워크림1K", "사워크림", "1kg×6PK/BOX", "BOX", 54000L,
                        56700L, "2026-08-10"));
        itemRepository.save(
                item(file, "DOC-008", 9L, SourceType.MANUAL, "한결유통", "라임30과", "라임", "30EA/BOX", "BOX", 30000L, 33000L,
                        "2026-08-03"));
        itemRepository.save(
                item(file, "DOC-009", 10L, SourceType.PDF, "가온푸드", "할라피뇨슬라이스", "할라피뇨 슬라이스", "3kg×6CAN/BOX", "BOX",
                        72000L, 72000L, "2026-08-01"));
        itemRepository.save(
                item(file, "DOC-010", 11L, SourceType.IMAGE, "가온푸드", "나초칩454G", "나초칩", "454g×12PK/BOX", "BOX", 48000L,
                        51000L, "2026-08-05"));
        itemRepository.save(
                item(file, "DOC-011", 12L, SourceType.XLSX, "가온푸드", "블랙빈2.5K", "블랙빈", "2.5kg×6CAN/BOX", "BOX", 60000L,
                        63000L, "2026-08-05"));
        itemRepository.save(
                item(file, "DOC-012", 13L, SourceType.MANUAL, "한결유통", "자스민쌀10K", "자스민쌀", "10kg/PO", "PO", 38000L,
                        39500L, "2026-08-01"));
        itemRepository.save(
                item(file, "DOC-013", 14L, SourceType.PDF, "새봄식품", "냉감튀2K", "냉동 감자튀김", "2kg×6PK/BOX", "BOX", 45000L,
                        47500L, "2026-08-07"));
        itemRepository.save(
                item(file, "DOC-014", 15L, SourceType.PDF, "가온푸드", "스모크BBQ소스", "스모크 바비큐소스", "2kg×6PK/BOX", "BOX",
                        58000L, 60900L, "2026-08-07"));
        itemRepository.save(
                item(file, "DOC-015", 16L, SourceType.XLSX, "푸른포장", "종이보울500", "종이 보울", "500EA/BOX", "BOX", 52000L,
                        55000L, "2026-08-12"));
        itemRepository.save(
                itemNoDate(file, "DOC-016", 17L, SourceType.IMAGE, "푸른포장", "투명리드500", "투명 리드", "500EA/BOX", "BOX",
                        39000L, 41000L));
        itemRepository.save(
                item(file, "DOC-017", 18L, SourceType.PDF, "바다원", "냉동새우살900", "냉동 새우살", "900g×10PK/BOX", "BOX", 125000L,
                        132000L, "2026-08-15"));
        itemRepository.save(
                item(file, "DOC-018", 19L, SourceType.PDF, "바다원", "냉동새우살900", "냉동 새우살", "900g×10PK/BOX", "BOX", 125000L,
                        132000L, "2026-08-15"));
        itemRepository.save(
                item(file, "DOC-019", 20L, SourceType.XLSX, "새봄식품", "냉동돈전지", "냉동 돼지고기 전지", "기존 10kg / 변경 9kg", "BOX",
                        86000L, 86000L, "2026-08-15"));
        itemRepository.save(
                item(file, "DOC-020", 21L, SourceType.MANUAL, "한결유통", "고수4단", "고수", "기존 1kg / 변경 4단", "KG/단", 28000L,
                        22000L, "2026-08-03"));

        log.info("[SampleData] CSV 20건 초기화 완료");
    }

    private Item item(File file, String docId, Long rowNo, SourceType sourceType, String supplierName,
                      String rawItemName, String normalizedItemName, String spec, String unit,
                      Long priceBefore, Long priceAfter, String effectiveDate) {
        return Item.builder()
                .file(file)
                .docId(docId)
                .rowNo(rowNo)
                .sourceType(sourceType)
                .supplierName(supplierName)
                .rawItemName(rawItemName)
                .normalizedItemName(normalizedItemName)
                .spec(spec)
                .unit(unit)
                .priceBefore(priceBefore)
                .priceAfter(priceAfter)
                .effectiveDate(LocalDate.parse(effectiveDate))
                .reviewStatus(ReviewStatus.NEW)
                .build();
    }

    private Item itemNoDate(File file, String docId, Long rowNo, SourceType sourceType, String supplierName,
                            String rawItemName, String normalizedItemName, String spec, String unit,
                            Long priceBefore, Long priceAfter) {
        return Item.builder()
                .file(file)
                .docId(docId)
                .rowNo(rowNo)
                .sourceType(sourceType)
                .supplierName(supplierName)
                .rawItemName(rawItemName)
                .normalizedItemName(normalizedItemName)
                .spec(spec)
                .unit(unit)
                .priceBefore(priceBefore)
                .priceAfter(priceAfter)
                .reviewStatus(ReviewStatus.NEW)
                .build();
    }
}
