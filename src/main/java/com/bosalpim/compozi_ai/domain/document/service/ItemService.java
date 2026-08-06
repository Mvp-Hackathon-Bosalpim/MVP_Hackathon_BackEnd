package com.bosalpim.compozi_ai.domain.document.service;

import com.bosalpim.compozi_ai.domain.document.dto.request.manualFile.CreateManualItemDocumentListReqDto;
import com.bosalpim.compozi_ai.domain.document.dto.request.manualFile.CreateManualItemDocumentReqDto;
import com.bosalpim.compozi_ai.domain.document.entity.File;
import com.bosalpim.compozi_ai.domain.document.entity.Item;
import com.bosalpim.compozi_ai.domain.document.repository.ItemRepository;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;

    public void createManualItem(CreateManualItemDocumentListReqDto reqDto, List<File> savedFiles) {
        List<CreateManualItemDocumentReqDto> itemDtos = reqDto.getItems();
        List<Item> items = new ArrayList<>();

        for (int i = 0; i < itemDtos.size(); i++) {
            File file = savedFiles.get(i);
            CreateManualItemDocumentReqDto itemDto = itemDtos.get(i);
            items.add(Item.CreateManualItem(itemDto, file));
        }

        itemRepository.saveAll(items);
    }
}
