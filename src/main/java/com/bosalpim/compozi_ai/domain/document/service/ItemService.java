package com.bosalpim.compozi_ai.domain.document.service;

import com.bosalpim.compozi_ai.domain.document.component.validator.ItemDocumentValidator;
import com.bosalpim.compozi_ai.domain.document.dto.request.commonFile.CreateCommonItemDocumentReqDto;
import com.bosalpim.compozi_ai.domain.document.dto.request.manualFile.CheckDuplicatedManualItemDto;
import com.bosalpim.compozi_ai.domain.document.dto.request.manualFile.CreateManualItemDocumentListReqDto;
import com.bosalpim.compozi_ai.domain.document.dto.request.manualFile.CreateManualItemDocumentReqDto;
import com.bosalpim.compozi_ai.domain.document.entity.File;
import com.bosalpim.compozi_ai.domain.document.entity.Item;
import com.bosalpim.compozi_ai.domain.document.repository.ItemRepository;
import com.bosalpim.compozi_ai.domain.inbox.entity.DuplicatedGroup;
import com.bosalpim.compozi_ai.domain.inbox.repository.DuplicatedGroupRepository;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;
    private final DuplicatedGroupRepository duplicatedGroupRepository;
    private final ItemDocumentValidator itemDocumentValidator;

    public List<Item> createCommonItem(List<CreateCommonItemDocumentReqDto> reqDtos, File savedFile) {
        itemDocumentValidator.markDuplicatesForCommon(reqDtos);

        // 중복 탐지
        // TODO :  1. 필드 누락은 (Bean Validation) 사용 추천, 2. 규격, 단위 처리,
        //  3. 해당 이상 탐지에 대한 이슈 테이블 생성 필요 + item 상태변화 구현 필요
        return processAndSaveItems(
                reqDtos.size(),
                i -> reqDtos.get(i).getDuplicateGroupKey(),
                (i, group) -> Item.CreateCommonItem(reqDtos.get(i), savedFile, group)
        );
    }

    public List<Item> createManualItem(CreateManualItemDocumentListReqDto reqDtos, List<File> savedFiles) {
        List<CreateManualItemDocumentReqDto> itemDtos = reqDtos.getItems();
        List<CheckDuplicatedManualItemDto> checkedDtos = itemDocumentValidator.markDuplicatesForManual(itemDtos);

        return processAndSaveItems(
                itemDtos.size(),
                i -> checkedDtos.get(i).getDuplicateGroupKey(),
                (i, group) -> Item.CreateManualItem(
                        itemDtos.get(i),
                        savedFiles.get(i),
                        checkedDtos.get(i).getNormalizedItemName(),
                        group
                )
        );
    }

    private List<Item> processAndSaveItems(int size, Function<Integer, String> keyExtractor,
                                           BiFunction<Integer, DuplicatedGroup, Item> itemMapper) {
        Map<String, DuplicatedGroup> groupMap = new HashMap<>();
        List<Item> items = new ArrayList<>();

        for (int i = 0; i < size; i++) {
            String duplicateKey = keyExtractor.apply(i);
            DuplicatedGroup group = null;

            if (duplicateKey != null) {
                group = groupMap.computeIfAbsent(duplicateKey, key -> DuplicatedGroup.create());
            }

            items.add(itemMapper.apply(i, group));
        }

        if (!groupMap.isEmpty()) {
            duplicatedGroupRepository.saveAll(groupMap.values());
        }

        return itemRepository.saveAll(items);
    }
}
