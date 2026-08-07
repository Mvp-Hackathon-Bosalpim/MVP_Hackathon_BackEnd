package com.bosalpim.compozi_ai.domain.document.entity;

import com.bosalpim.compozi_ai.domain.document.enums.InputMethod;
import com.bosalpim.compozi_ai.general.entity.BaseTimeStampEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "files")
public class File extends BaseTimeStampEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fileName;

    @Enumerated(value = EnumType.STRING)
    @Column(nullable = false)
    private InputMethod inputMethod;

    @Builder
    public File(String fileName, InputMethod inputMethod) {
        this.fileName = fileName;
        this.inputMethod = inputMethod;
    }

    public static File createFile(String fileName, InputMethod inputMethod) {
        return File.builder()
                .fileName(fileName)
                .inputMethod(inputMethod)
                .build();
    }
}
