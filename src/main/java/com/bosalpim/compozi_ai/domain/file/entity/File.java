package com.bosalpim.compozi_ai.domain.file.entity;

import com.bosalpim.compozi_ai.domain.file.enums.InputMethod;
import com.bosalpim.compozi_ai.general.entity.BaseTimeStampEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "files")
public class File extends BaseTimeStampEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fileName;

    @Enumerated(value = EnumType.STRING)
    private InputMethod inputMethod;


}
