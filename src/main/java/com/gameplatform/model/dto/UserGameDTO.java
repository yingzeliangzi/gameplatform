package com.gameplatform.model.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * @author SakurazawaRyoko
 * @version 1.0
 * @date 2024/12/29 16:13
 * @description TODO
 */
@Data
public class UserGameDTO {
    private Long id;
    private Long gameId;
    private String gameTitle;
    private String gameCoverImage;
    private Double userRating;
    private String userReview;
    private LocalDateTime purchasedAt;
    private LocalDateTime lastPlayedAt;
    private Integer playTime;
    private Double gameRating;
    private Integer ratingCount;
}