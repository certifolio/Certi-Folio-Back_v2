package com.certifolio.server.Form.util;

import java.time.LocalDate;

public class DateUtils {

    /**
     * 날짜 문자열을 LocalDate로 변환
     * YYYY-MM 또는 YYYY.MM 형식을 YYYY-MM-01로 변환
     */
    public static LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) {
            return null;
        }
        try {
            // YYYY.MM 형식을 YYYY-MM로 변환
            String normalizedDateStr = dateStr.replace(".", "-");
            
            // YYYY-MM 형식
            if (normalizedDateStr.matches("\\d{4}-\\d{2}")) {
                return LocalDate.parse(normalizedDateStr + "-01");
            }
            // YYYY-MM-DD 형식
            return LocalDate.parse(normalizedDateStr);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid date format: " + dateStr);
        }
    }

    /**
     * LocalDate를 문자열로 변환
     */
    public static String dateToString(LocalDate date) {
        return date != null ? date.toString() : null;
    }
}
