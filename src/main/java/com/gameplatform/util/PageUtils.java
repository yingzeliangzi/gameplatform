package com.gameplatform.util;

import lombok.experimental.UtilityClass;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;

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
        return new PageImpl<>(
                list.subList(start, end),
                pageable,
                list.size()
        );
    }
}
