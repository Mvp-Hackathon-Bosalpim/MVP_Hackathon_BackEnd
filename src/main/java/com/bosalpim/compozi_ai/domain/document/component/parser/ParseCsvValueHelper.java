package com.bosalpim.compozi_ai.domain.document.component.parser;

import java.time.LocalDate;

public class ParseCsvValueHelper {

    public static String parseString(String val) { // 문자열 변환
        return val;
    }

    public static Long parseLong(String val, ParseContext context) {
        String cleaned = parseString(val);
        if (cleaned == null) {
            return null;
        }

        try {
            return Long.parseLong(cleaned.replace(",", ""));
        } catch (Exception e) {
            context.setError();
            return null;
        }
    }

    public static LocalDate parseDate(String val, ParseContext context) { // 날짜 변환
        String cleaned = parseString(val);
        if (cleaned == null) {
            return null;
        }

        try {
            String formatted = cleaned.replace("/", "-");
            return LocalDate.parse(formatted);
        } catch (Exception e) {
            context.setError();
            return null;
        }
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
