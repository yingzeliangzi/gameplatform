package com.gameplatform.util;

import lombok.experimental.UtilityClass;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * @author SakurazawaRyoko
 * @version 1.0
 * @date 2024/12/29 15:14
 * @description TODO
 */
@UtilityClass
public class DateUtils {
    public static String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) return "";
        return DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(dateTime);
    }

    public static LocalDateTime parseDateTime(String dateTime) {
        if (StringUtils.isEmpty(dateTime)) return null;
        return LocalDateTime.parse(dateTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
}
