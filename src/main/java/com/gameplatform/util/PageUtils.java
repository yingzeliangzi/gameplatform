package com.gameplatform.util;

import lombok.experimental.UtilityClass;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author SakurazawaRyoko
 * @version 1.0
 * @date 2024/12/29 15:13
 * @description TODO
 */
@UtilityClass
public class PageUtils {
    public static <T> PageImpl<T> toPage(List<T> list, Pageable pageable) {
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), list.size());
        return new PageImpl<>(list.subList(start, end), pageable, list.size());
    }

    public static <T, R> Page<R> convertPage(Page<T> page, Function<T, R> converter) {
        List<R> converted = page.getContent().stream()
                .map(converter)
                .collect(Collectors.toList());
        return new PageImpl<>(converted, page.getPageable(), page.getTotalElements());
    }
}