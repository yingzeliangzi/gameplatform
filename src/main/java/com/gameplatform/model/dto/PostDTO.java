package com.gameplatform.model.dto;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author SakurazawaRyoko
 * @version 1.0
 * @date 2024/12/28 19:21
 * @description TODO
 */
@Data
public class PostDTO {
    private Long id;

    @NotBlank(message = "标题不能为空")
    @Size(min = 5, max = 100, message = "标题长度必须在5-100之间")
    private String title;

    @NotBlank(message = "内容不能为空")
    @Size(min = 10, message = "内容长度不能少于10个字符")
    private String content;

    private Long gameId;
    private String gameName;
    private UserDTO author;
    private List<CommentDTO> comments;
    private Integer viewCount;
    private Integer likeCount;
    private LocalDateTime createdAt;
}