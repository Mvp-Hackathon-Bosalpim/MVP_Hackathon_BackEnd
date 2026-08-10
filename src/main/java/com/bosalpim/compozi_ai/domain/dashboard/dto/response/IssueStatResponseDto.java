package com.bosalpim.compozi_ai.domain.dashboard.dto.response;

import com.bosalpim.compozi_ai.domain.dashboard.dto.IssueTypeCountDto;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class IssueStatResponseDto {
    private final List<IssueTypeCountDto> stats;
}