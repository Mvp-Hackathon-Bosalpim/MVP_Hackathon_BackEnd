package com.bosalpim.compozi_ai.domain.document.component.parser;

public class OcrNormalizeUtils {


    public static String formatSpecWithMultiplySymbol(String input) {
        if (input == null || input.isBlank()) {
            return "";
        }

        return input
                .replaceAll("(?i)BO[×xX]", "BOX")
                .replaceAll("(?<=\\d)[xX](?=\\d)", "×")
                .replaceAll("(?<=\\d)[xX]", "×")
                .replaceAll("[xX](?=\\d)", "×")
                .trim();
    }
}
