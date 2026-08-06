package com.bosalpim.compozi_ai.domain.document.service;

import com.bosalpim.compozi_ai.domain.document.dto.request.manualFile.CreateManualItemDocumentListReqDto;
import com.bosalpim.compozi_ai.domain.document.dto.response.CreateItemDocumentResDto;
import com.bosalpim.compozi_ai.domain.document.entity.File;
import com.bosalpim.compozi_ai.domain.document.enums.InputMethod;
import com.bosalpim.compozi_ai.domain.document.repository.FileRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FileService {

    private final FileRepository fileRepository;
    private final ItemService itemService;

    public CreateItemDocumentResDto createManualFile(CreateManualItemDocumentListReqDto reqDto) {
        List<File> files = reqDto.getItems().stream()
                .map(req -> File.createFile(null, InputMethod.MANUAL))
                .toList();

        List<File> savedFiles = fileRepository.saveAll(files);
        itemService.createManualItem(reqDto, savedFiles);

        return CreateItemDocumentResDto.from(savedFiles);

    }
}
