package com.gameplatform.model.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class CommentDTO {
    private Long id;

    @NotBlank(message = "评论内容不能为空")
    private String content;

    private Long postId;
    private Long authorId;
    private String authorName;
    private String authorAvatar;
    private Long parentId;
    private Integer likeCount;
    private boolean isReported;
    private LocalDateTime createdAt;
    private List<CommentDTO> replies;
}