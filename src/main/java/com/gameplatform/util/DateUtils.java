package com.gameplatform.util;

import lombok.experimental.UtilityClass;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Date;

/**
 * @author SakurazawaRyoko
 * @version 1.0
 * @date 2024/12/29 15:14
 * @description TODO
 */
@UtilityClass
public class DateUtils {

    private static final String DEFAULT_DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd";
    private static final String DEFAULT_TIME_FORMAT = "HH:mm:ss";
    private static final String SIMPLE_DATE_FORMAT = "MM/dd";
    private static final String YEAR_MONTH_FORMAT = "yyyy-MM";

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(DEFAULT_DATE_TIME_FORMAT);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern(DEFAULT_DATE_FORMAT);
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern(DEFAULT_TIME_FORMAT);
    private static final DateTimeFormatter SIMPLE_DATE_FORMATTER = DateTimeFormatter.ofPattern(SIMPLE_DATE_FORMAT);
    private static final DateTimeFormatter YEAR_MONTH_FORMATTER = DateTimeFormatter.ofPattern(YEAR_MONTH_FORMAT);

    public static String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) return "";
        return DATE_TIME_FORMATTER.format(dateTime);
    }

    public static String formatDate(LocalDateTime dateTime) {
        if (dateTime == null) return "";
        return DATE_FORMATTER.format(dateTime);
    }

    public static String formatTime(LocalDateTime dateTime) {
        if (dateTime == null) return "";
        return TIME_FORMATTER.format(dateTime);
    }

    public static String formatSimpleDate(LocalDateTime dateTime) {
        if (dateTime == null) return "";
        return SIMPLE_DATE_FORMATTER.format(dateTime);
    }

    public static String formatYearMonth(LocalDateTime dateTime) {
        if (dateTime == null) return "";
        return YEAR_MONTH_FORMATTER.format(dateTime);
    }

    public static LocalDateTime parseDateTime(String dateTime) {
        if (!StringUtils.hasText(dateTime)) return null;
        return LocalDateTime.parse(dateTime, DATE_TIME_FORMATTER);
    }

    public static LocalDate parseDate(String date) {
        if (!StringUtils.hasText(date)) return null;
        return LocalDate.parse(date, DATE_FORMATTER);
    }

    public static String formatFriendlyTime(LocalDateTime dateTime) {
        if (dateTime == null) return "";

        LocalDateTime now = LocalDateTime.now();
        long minutes = ChronoUnit.MINUTES.between(dateTime, now);

        if (minutes < 1) return "刚刚";
        if (minutes < 60) return minutes + "分钟前";

        long hours = ChronoUnit.HOURS.between(dateTime, now);
        if (hours < 24) return hours + "小时前";

        long days = ChronoUnit.DAYS.between(dateTime, now);
        if (days < 7) return days + "天前";
        if (days < 30) return (days / 7) + "周前";

        long months = ChronoUnit.MONTHS.between(dateTime, now);
        if (months < 12) return months + "个月前";

        return ChronoUnit.YEARS.between(dateTime, now) + "年前";
    }

    public static String formatDuration(long seconds) {
        if (seconds < 60) return seconds + "秒";
        if (seconds < 3600) return (seconds / 60) + "分钟";
        if (seconds < 86400) return (seconds / 3600) + "小时";
        return (seconds / 86400) + "天";
    }

    public static LocalDateTime convertToLocalDateTime(Date date) {
        if (date == null) return null;
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    public static Date convertToDate(LocalDateTime localDateTime) {
        if (localDateTime == null) return null;
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    public static boolean isSameDay(LocalDateTime date1, LocalDateTime date2) {
        if (date1 == null || date2 == null) return false;
        return date1.toLocalDate().isEqual(date2.toLocalDate());
    }

    public static boolean isToday(LocalDateTime date) {
        if (date == null) return false;
        return isSameDay(date, LocalDateTime.now());
    }

    public static boolean isFuture(LocalDateTime date) {
        if (date == null) return false;
        return date.isAfter(LocalDateTime.now());
    }

    public static boolean isPast(LocalDateTime date) {
        if (date == null) return false;
        return date.isBefore(LocalDateTime.now());
    }
}