package com.bosalpim.compozi_ai.domain.file.repository;

import com.bosalpim.compozi_ai.domain.file.entity.File;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileRepository extends JpaRepository<File, Long> {
}
