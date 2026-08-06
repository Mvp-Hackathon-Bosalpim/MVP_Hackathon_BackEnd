package com.bosalpim.compozi_ai.domain.file.dto.response.manualFile;

import com.bosalpim.compozi_ai.domain.file.entity.File;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CreateManualItemDocumentResDto {
    @JsonProperty("file_id")
    private final List<Long> fileIds;

    public static CreateManualItemDocumentResDto from(List<File> files) {
        return CreateManualItemDocumentResDto.builder()
                .fileIds(
                        files.stream()
                                .map(File::getId)
                                .toList()
                )
                .build();
    }
}
