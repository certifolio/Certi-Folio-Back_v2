package com.certifolio.server.global.common.util;

import java.time.LocalDate;

public class DateUtils {

    private DateUtils() {}

    /**
     * 날짜 문자열을 LocalDate로 변환
     * YYYY-MM 또는 YYYY.MM 형식을 YYYY-MM-01로 변환
     */
    public static LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) {
            throw new IllegalArgumentException("Date string cannot be null or empty");
        }
        try {
            String normalizedDateStr = dateStr.replace(".", "-");

            if (normalizedDateStr.matches("\\d{4}-\\d{2}")) {
                return LocalDate.parse(normalizedDateStr + "-01");
            }
            return LocalDate.parse(normalizedDateStr);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid date format: " + dateStr);
        }
    }

    /**
     * LocalDate를 YYYY-MM 문자열로 변환
     */
    public static String dateToString(LocalDate date) {
        return date != null ? date.toString().substring(0, 7) : null;
    }
}
