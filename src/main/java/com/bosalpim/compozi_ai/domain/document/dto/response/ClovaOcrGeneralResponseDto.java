package com.bosalpim.compozi_ai.domain.document.dto.response;

import java.util.List;

public record ClovaOcrGeneralResponseDto(
        List<ImageResult> images
) {
    public record ImageResult(
            String uid,
            String name,
            String inferResult,
            List<Field> fields
    ) {
    }

    public record Field(
            String inferText,
            BoundingPoly boundingPoly
    ) {
    }

    public record BoundingPoly(
            List<Vertex> vertices
    ) {
    }

    public record Vertex(
            double x,
            double y
    ) {
    }
}
