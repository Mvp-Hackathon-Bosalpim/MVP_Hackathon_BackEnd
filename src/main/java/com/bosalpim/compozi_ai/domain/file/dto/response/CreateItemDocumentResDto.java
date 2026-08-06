package com.bosalpim.compozi_ai.domain.file.dto.response.manualFile;

import com.bosalpim.compozi_ai.domain.file.entity.File;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CreateManualItemDocumentResDto {


    private final Integer total;
    private final Integer normal;

    @JsonProperty("need_checked")
    private final Integer needCheck;


    public static CreateManualItemDocumentResDto from(List<File> savedFiles) {
        return CreateManualItemDocumentResDto.builder()
                .total(savedFiles.size())
                .normal(1)
                .needCheck(savedFiles.size() - 1)
                .build();
        // TODO : 추후 데이터 유효성 검사 로직 구현 시 수정!

    }
}
