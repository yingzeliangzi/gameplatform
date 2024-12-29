package com.gameplatform.service.impl;

import com.gameplatform.exception.BusinessException;
import com.gameplatform.model.dto.CommentDTO;
import com.gameplatform.model.entity.Comment;
import com.gameplatform.model.entity.Post;
import com.gameplatform.model.entity.Report;
import com.gameplatform.model.entity.User;
import com.gameplatform.repository.CommentRepository;
import com.gameplatform.repository.PostRepository;
import com.gameplatform.repository.ReportRepository;
import com.gameplatform.repository.UserRepository;
import com.gameplatform.service.CommentService;
import com.gameplatform.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author SakurazawaRyoko
 * @version 1.0
 * @date 2024/12/29 16:17
 * @description TODO
 */
@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final ReportRepository reportRepository;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public CommentDTO createComment(Long userId, CommentDTO commentDTO) {
        User author = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));

        Post post = postRepository.findById(commentDTO.getPostId())
                .orElseThrow(() -> new BusinessException("帖子不存在"));

        Comment comment = new Comment();
        comment.setContent(commentDTO.getContent());
        comment.setAuthor(author);
        comment.setPost(post);

        if (commentDTO.getParentId() != null) {
            Comment parentComment = commentRepository.findById(commentDTO.getParentId())
                    .orElseThrow(() -> new BusinessException("父评论不存在"));
            comment.setParentComment(parentComment);
        }

        Comment savedComment = commentRepository.save(comment);

        // 发送评论通知
        if (comment.getParentComment() != null) {
            notificationService.sendCommentReplyNotification(
                    comment.getParentComment().getAuthor(),
                    post,
                    author.getNickname()
            );
        } else {
            notificationService.sendNewCommentNotification(
                    post.getAuthor(),
                    post,
                    author.getNickname()
            );
        }

        return convertToDTO(savedComment);
    }

    @Override
    @Transactional
    public CommentDTO updateComment(Long commentId, Long userId, String content) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new BusinessException("评论不存在"));

        if (!comment.getAuthor().getId().equals(userId)) {
            throw new BusinessException("无权修改此评论");
        }

        comment.setContent(content);
        return convertToDTO(commentRepository.save(comment));
    }

    @Override
    @Transactional
    public void deleteComment(Long commentId, Long userId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new BusinessException("评论不存在"));

        if (!comment.getAuthor().getId().equals(userId) &&
                !userRepository.findById(userId).get().isAdmin()) {
            throw new BusinessException("无权删除此评论");
        }

        commentRepository.delete(comment);
    }

    @Override
    @Transactional(readOnly = true)
    public CommentDTO getCommentById(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new BusinessException("评论不存在"));
        return convertToDTO(comment);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CommentDTO> getPostComments(Long postId, Pageable pageable) {
        return commentRepository.findByPostIdAndParentCommentIsNull(postId, pageable)
                .map(this::convertToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CommentDTO> getUserComments(Long userId, Pageable pageable) {
        return commentRepository.findByAuthorId(userId, pageable)
                .map(this::convertToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentDTO> getCommentReplies(Long commentId) {
        return commentRepository.findByParentCommentId(commentId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void likeComment(Long commentId, Long userId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new BusinessException("评论不存在"));

        comment.setLikeCount(comment.getLikeCount() + 1);
        commentRepository.save(comment);

        // 可以在这里添加点赞记录和通知
    }

    @Override
    @Transactional
    public void unlikeComment(Long commentId, Long userId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new BusinessException("评论不存在"));

        if (comment.getLikeCount() > 0) {
            comment.setLikeCount(comment.getLikeCount() - 1);
            commentRepository.save(comment);
        }
    }

    @Override
    @Transactional
    public void reportComment(Long commentId, Long userId, String reason) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new BusinessException("评论不存在"));
        User reporter = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));

        if (reportRepository.existsByReporterIdAndTypeAndTargetIdAndStatus(
                userId, Report.ReportType.COMMENT, commentId, Report.ReportStatus.PENDING)) {
            throw new BusinessException("该评论已被举报");
        }

        Report report = new Report();
        report.setReporter(reporter);
        report.setType(Report.ReportType.COMMENT);
        report.setTargetId(commentId);
        report.setReason(reason);
        reportRepository.save(report);

        comment.setReported(true);
        commentRepository.save(comment);
    }

    private CommentDTO convertToDTO(Comment comment) {
        CommentDTO dto = new CommentDTO();
        BeanUtils.copyProperties(comment, dto);
        dto.setAuthorId(comment.getAuthor().getId());
        dto.setAuthorName(comment.getAuthor().getNickname());
        dto.setAuthorAvatar(comment.getAuthor().getAvatar());
        dto.setPostId(comment.getPost().getId());

        if (comment.getParentComment() != null) {
            dto.setParentId(comment.getParentComment().getId());
        }

        if (comment.getReplies() != null && !comment.getReplies().isEmpty()) {
            dto.setReplies(comment.getReplies().stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList()));
        }

        return dto;
    }
}
