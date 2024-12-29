package com.gameplatform.model.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Set;

/**
 * @author SakurazawaRyoko
 * @version 1.0
 * @date 2024/12/29 14:09
 * @description TODO
 */
@Data
public class GameDTO {
    private Long id;
    private String title;
    private String description;
    private BigDecimal price;
    private String coverImage;
    private Set<String> screenshots;
    private Set<String> categories;
    private Double rating;
    private Integer ratingCount;
    private Integer popularity;
    private Long gameId;
    private String gameName;
}
