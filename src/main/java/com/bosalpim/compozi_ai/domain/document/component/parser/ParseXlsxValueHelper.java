package com.bosalpim.compozi_ai.domain.document.component.parser;

import java.time.LocalDate;
import java.time.ZoneId;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;


public class ParseXlsxValueHelper {

    public static String parseString(Cell cell) {
        if (cell == null) {
            return null;
        }
        return switch (cell.getCellType()) {
            case STRING -> {
                String val = cell.getStringCellValue().trim();
                yield val.isBlank() ? null : val; // break 대신 yield 를 사용해서 반환 및 종료 동시에 가능
            }
            case NUMERIC -> String.valueOf((long) cell.getNumericCellValue());
            default -> null;
        };
    }


    public static Long parseLong(Cell cell, ParseContext context) {
        if (cell == null) {
            return null;
        }

        try {

            if (cell.getCellType() == CellType.NUMERIC) {
                return (long) cell.getNumericCellValue();
            }

            if (cell.getCellType() == CellType.STRING) {
                String text = cell.getStringCellValue().trim();
                if (text.isBlank()) {
                    return null;
                }
                return Long.parseLong(text.replace(",", ""));
            }
        } catch (Exception e) {
            context.setError();
            return null;
        }

        return null;
    }

    public static LocalDate parseDate(Cell cell, ParseContext context) {
        if (cell == null) {
            return null;
        }

        try {
            if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
                return cell.getDateCellValue().toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate();
            }

            if (cell.getCellType() == CellType.STRING) {
                String text = cell.getStringCellValue().trim();
                if (text.isBlank()) {
                    return null;
                }
                return LocalDate.parse(text.replace("/", "-"));
            }
        } catch (Exception e) {
            context.setError();
            return null;
        }

        return null;
    }

    public static class ParseContext {
        private boolean hasError = false;

        public void setError() {
            this.hasError = true;
        }

        public boolean hasError() {
            return hasError;
        }
    }
}
