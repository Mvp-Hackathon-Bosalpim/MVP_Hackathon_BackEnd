package com.bosalpim.compozi_ai.domain.item.service;

import com.bosalpim.compozi_ai.domain.file.dto.request.manualFile.CreateManualItemDocumentListReqDto;
import com.bosalpim.compozi_ai.domain.item.entity.Item;
import com.bosalpim.compozi_ai.domain.item.repository.ItemRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;

    public void createItem(CreateManualItemDocumentListReqDto reqDto) {
        List<Item> items = reqDto.getItems().stream()
                .map(Item::CreateManualItem)
                .toList();
        itemRepository.saveAll(items);
    }
}
