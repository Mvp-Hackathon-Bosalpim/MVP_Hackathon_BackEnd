package com.bosalpim.compozi_ai.domain.document.dto.response;

import java.util.List;

public record ClovaOcrGeneralResponseDto(
        String version,
        String requestId,
        long timestamp,
        List<Image> images
) {
    public record Image(
            String uid,
            String name,
            String inferResult,
            String message,
            ValidationResult validationResult,
            ConvertedImageInfo convertedImageInfo,
            List<Field> fields
    ) {
    }

    public record ValidationResult(
            String result
    ) {
    }

    public record ConvertedImageInfo(
            int width,
            int height,
            int pageIndex,
            boolean longImage
    ) {
    }

    public record Field(
            String valueType,
            BoundingPoly boundingPoly,
            String inferText,
            double inferConfidence,
            String type,
            Boolean lineBreak
    ) {
    }

    public record BoundingPoly(
            List<Vertex> vertices
    ) {
    }

    public record Vertex(
            Double x,
            Double y
    ) {
    }
}
