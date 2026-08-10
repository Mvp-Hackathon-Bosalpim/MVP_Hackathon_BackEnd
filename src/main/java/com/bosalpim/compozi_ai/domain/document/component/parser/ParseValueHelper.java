package com.bosalpim.compozi_ai.domain.document.component.parser;

import java.time.LocalDate;
import org.springframework.stereotype.Component;

@Component
public class ParseValueHelper {

    public static String parseString(String val) { // 문자열 변환
        return val;
    }

    public static Long parseLong(String val, ParseContext context) { // 정수 변환
        String cleaned = parseString(val);
        if (cleaned == null) {
            return null;
        }

        try {
            return Long.parseLong(cleaned.replace(",", ""));
        } catch (Exception e) {
            context.setError(); // 파싱 에러 감지
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
            context.setError(); // 파싱 에러 감지
            return null;
        }
    }

    // 행별 에러 상태 감지용 컨텍스트 클래스
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
