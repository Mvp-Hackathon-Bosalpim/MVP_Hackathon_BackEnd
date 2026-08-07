package com.bosalpim.compozi_ai.domain.document.service;

import com.bosalpim.compozi_ai.domain.document.component.mapper.ItemNameMapper;
import com.bosalpim.compozi_ai.domain.document.component.validator.ItemDocumentValidator;
import com.bosalpim.compozi_ai.domain.document.dto.request.commonFile.CreateCommonItemDocumentReqDto;
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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;
    private final DuplicatedGroupRepository duplicatedGroupRepository; // 일단 레포 가져오기
    private final ItemDocumentValidator itemDocumentValidator;
    private final ItemNameMapper itemNameMapper;


    public List<Item> createCommonItem(List<CreateCommonItemDocumentReqDto> reqDtos, File savedFile) {

        itemDocumentValidator.markDuplicates(reqDtos);

        Map<String, DuplicatedGroup> groupMap = new HashMap<>();
        List<Item> items = new ArrayList<>();

        for (CreateCommonItemDocumentReqDto dto : reqDtos) {
            DuplicatedGroup group = null;

            // 마킹된 중복키가 있는 경우 (2번째 이후 건)
            if (dto.getDuplicateGroupKey() != null) {
                group = groupMap.computeIfAbsent(
                        dto.getDuplicateGroupKey(),
                        key -> DuplicatedGroup.create()
                );
            }

            Item item = Item.CreateCommonItem(dto, savedFile, group);
            items.add(item);
        }
        if (!groupMap.isEmpty()) {
            duplicatedGroupRepository.saveAll(groupMap.values());
        }

        return itemRepository.saveAll(items);
    }

    public List<Item> createManualItem(CreateManualItemDocumentListReqDto reqDto, List<File> savedFiles) {
        List<CreateManualItemDocumentReqDto> itemDtos = reqDto.getItems();
        List<Item> items = new ArrayList<>();

        for (int i = 0; i < itemDtos.size(); i++) {
            File file = savedFiles.get(i);
            CreateManualItemDocumentReqDto itemDto = itemDtos.get(i);
            String normalizedName = itemNameMapper.map(itemDto.getRawItemName());
            items.add(Item.CreateManualItem(itemDto, file, normalizedName));
        }

        return itemRepository.saveAll(items);
    }
}
