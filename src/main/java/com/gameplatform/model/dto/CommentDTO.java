package com.gameplatform.model.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class CommentDTO {
    private Long id;

    @NotBlank(message = "评论内容不能为空")
    @Size(min = 1, max = 500, message = "评论长度必须在1-500之间")
    private String content;

    private UserDTO author;
    private Long parentId;
    private List<CommentDTO> replies;
    private Integer likeCount;
    private LocalDateTime createdAt;
}
