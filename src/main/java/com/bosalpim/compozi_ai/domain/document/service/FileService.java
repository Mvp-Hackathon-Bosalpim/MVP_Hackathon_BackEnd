package com.bosalpim.compozi_ai.domain.document.service;

import com.bosalpim.compozi_ai.domain.document.component.parser.FileParser;
import com.bosalpim.compozi_ai.domain.document.dto.request.manualFile.CreateManualItemDocumentListReqDto;
import com.bosalpim.compozi_ai.domain.document.dto.response.CreateItemDocumentResDto;
import com.bosalpim.compozi_ai.domain.document.entity.File;
import com.bosalpim.compozi_ai.domain.document.entity.Item;
import com.bosalpim.compozi_ai.domain.document.enums.InputMethod;
import com.bosalpim.compozi_ai.domain.document.repository.FileRepository;
import com.bosalpim.compozi_ai.general.enums.BadStatusCode;
import com.bosalpim.compozi_ai.general.exception.CustomException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class FileService {

    private final List<FileParser> fileParsers;
    private final FileRepository fileRepository;
    private final ItemService itemService;


    @Transactional
    public CreateItemDocumentResDto createCommonFile(MultipartFile file) {
        String filename = file.getOriginalFilename(); // 파일 이름 추출

        FileParser parser = fileParsers.stream()
                .filter(p -> p.supports(filename))
                .findFirst()
                .orElseThrow(() -> new CustomException(BadStatusCode.UNSUPPORTED_FILE_TYPE));
        // 형식에 맞는 파서 추출

        File savedFile = fileRepository.save(File.createFile(filename, InputMethod.FILE)); // 파일 정보 저장

        // TODO : 여기쯤에 1. 정규화 품목명 기능 , 2. 유효성 검사 (규격 불일치, 단위 불일치, 중복, 필수 데이터 누락)

        List<Item> items = itemService.createCommonItem(parser.parse(file), savedFile); // 각 아이템 (row) 정보 저장 -> 인라인화 예정
        return CreateItemDocumentResDto.from(items);

    }


    public CreateItemDocumentResDto createManualFile(CreateManualItemDocumentListReqDto reqDto) {
        List<File> files = reqDto.getItems().stream()
                .map(req -> File.createFile(null, InputMethod.MANUAL))
                .toList();

        List<File> savedFiles = fileRepository.saveAll(files);
        return CreateItemDocumentResDto.from(itemService.createManualItem(reqDto, savedFiles));

    }
}
