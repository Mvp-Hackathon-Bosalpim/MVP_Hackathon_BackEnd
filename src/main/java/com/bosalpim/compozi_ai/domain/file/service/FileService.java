package com.bosalpim.compozi_ai.domain.file.service;

import com.bosalpim.compozi_ai.domain.file.dto.request.manualFile.CreateManualItemDocumentListReqDto;
import com.bosalpim.compozi_ai.domain.file.dto.response.manualFile.CreateManualItemDocumentResDto;
import com.bosalpim.compozi_ai.domain.file.entity.File;
import com.bosalpim.compozi_ai.domain.file.enums.InputMethod;
import com.bosalpim.compozi_ai.domain.file.repository.FileRepository;
import com.bosalpim.compozi_ai.domain.item.service.ItemService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FileService {

    private final FileRepository fileRepository;
    private final ItemService itemService;

    public CreateManualItemDocumentResDto createManualFile(CreateManualItemDocumentListReqDto reqDto) {
        List<File> files = reqDto.getItems().stream()
                .map(req -> File.createFile(null, InputMethod.MANUAL))
                .toList();

        List<File> savedFiles = fileRepository.saveAll(files);
        itemService.createManualItem(reqDto, savedFiles);

        return CreateManualItemDocumentResDto.from(savedFiles);

    }
}
