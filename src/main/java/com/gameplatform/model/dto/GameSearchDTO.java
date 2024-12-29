package com.gameplatform.model.dto;

import lombok.Data;

import java.util.Set;

/**
 * @author SakurazawaRyoko
 * @version 1.0
 * @date 2024/12/29 14:10
 * @description TODO
 */
@Data
public class GameSearchDTO {
    private String title;
    private Set<String> categories;
    private Double minRating;
}