package com.gameplatform.service.impl;

import com.gameplatform.annotation.RequirePermission;
import com.gameplatform.exception.BusinessException;
import com.gameplatform.model.dto.CommentDTO;
import com.gameplatform.model.dto.PostDTO;
import com.gameplatform.model.dto.ReportDTO;
import com.gameplatform.model.dto.UserDTO;
import com.gameplatform.model.entity.*;
import com.gameplatform.repository.*;
import com.gameplatform.service.CacheService;
import com.gameplatform.service.NotificationService;
import com.gameplatform.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

/**
 * @author SakurazawaRyoko
 * @version 1.0
 * @date 2024/12/28 19:24
 * @description TODO
 */
@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final GameRepository gameRepository;
    private final CommentRepository commentRepository;
    private final ReportRepository reportRepository;
    private final PostLikeRepository postLikeRepository;
    private final NotificationService notificationService;
    private final CacheService cacheService;

    @Override
    @Transactional
    public void deletePost(Long postId, Long userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException("帖子不存在"));

        if (!post.getAuthor().getId().equals(userId) &&
                !userRepository.findById(userId).get().isAdmin()) {
            throw new BusinessException("无权删除此帖子");
        }

        postRepository.delete(post);
    }

    @Override
    @Transactional
    public PostDTO getPostById(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException("帖子不存在"));

        // 增加浏览量
        postRepository.incrementViewCount(postId);

        return convertToDTO(post);
    }

    @Override
    @Transactional
    public CommentDTO replyToComment(Long commentId, CommentDTO replyDTO, Long userId) {
        Comment parentComment = commentRepository.findById(commentId)
                .orElseThrow(() -> new BusinessException("评论不存在"));

        User author = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));

        Comment reply = new Comment();
        reply.setContent(replyDTO.getContent());
        reply.setAuthor(author);
        reply.setPost(parentComment.getPost());
        reply.setParentComment(parentComment);

        Comment savedReply = commentRepository.save(reply);

        // 如果回复的不是自己的评论，则发送通知
        if (!parentComment.getAuthor().getId().equals(userId)) {
            notificationService.sendReplyNotification(
                    parentComment.getAuthor(),
                    parentComment,
                    author
            );
        }

        return convertToCommentDTO(savedReply);
    }

    private CommentDTO convertToCommentDTO(Comment comment) {
        CommentDTO dto = new CommentDTO();
        BeanUtils.copyProperties(comment, dto);
        dto.setAuthorId(comment.getAuthor().getId());
        dto.setAuthorName(comment.getAuthor().getNickname());
        dto.setAuthorAvatar(comment.getAuthor().getAvatar());
        dto.setPostId(comment.getPost().getId());

        if (comment.getParentComment() != null) {
            dto.setParentId(comment.getParentComment().getId());
        }

        // 添加回复列表
        if (comment.getReplies() != null && !comment.getReplies().isEmpty()) {
            dto.setReplies(comment.getReplies().stream()
                    .map(this::convertToCommentDTO)
                    .collect(Collectors.toList()));
        }

        return dto;
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
    @Transactional
    public void collectPost(Long postId, Long userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException("帖子不存在"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));

        // 检查是否已收藏
        if (postLikeRepository.existsByPostIdAndUserId(postId, userId)) {
            throw new BusinessException("已经收藏过此帖子");
        }

        PostLike postLike = new PostLike();
        postLike.setPost(post);
        postLike.setUser(user);
        postLikeRepository.save(postLike);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PostDTO> getHotPosts(Pageable pageable) {
        return postRepository.findHotPosts(pageable)
                .map(this::convertToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PostDTO> getPostsByGame(Long gameId, Pageable pageable) {
        return postRepository.findByGameId(gameId, pageable)
                .map(this::convertToDTO);
    }

    @Override
    @Transactional
    public void reportContent(ReportDTO reportDTO, Long userId) {
        User reporter = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));

        // 检查是否已经举报过
        if (reportRepository.existsByReporterIdAndTypeAndTargetIdAndStatus(
                userId,
                Report.ReportType.valueOf(reportDTO.getType()),
                reportDTO.getTargetId(),
                Report.ReportStatus.PENDING)) {
            throw new BusinessException("已经举报过该内容");
        }

        Report report = new Report();
        report.setReporter(reporter);
        report.setType(Report.ReportType.valueOf(reportDTO.getType()));
        report.setTargetId(reportDTO.getTargetId());
        report.setReason(reportDTO.getReason());
        report.setDescription(reportDTO.getDescription());
        report.setStatus(Report.ReportStatus.PENDING);
        report.setCreatedAt(LocalDateTime.now());

        reportRepository.save(report);

        // 更新目标内容的举报状态
        switch (report.getType()) {
            case POST -> {
                Post post = postRepository.findById(report.getTargetId())
                        .orElseThrow(() -> new BusinessException("帖子不存在"));
                post.setReported(true);
                postRepository.save(post);
            }
            case COMMENT -> {
                Comment comment = commentRepository.findById(report.getTargetId())
                        .orElseThrow(() -> new BusinessException("评论不存在"));
                comment.setReported(true);
                commentRepository.save(comment);
            }
        }
    }

    @Override
    @Transactional
    public void likePost(Long postId, Long userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException("帖子不存在"));

        if (postLikeRepository.existsByPostIdAndUserId(postId, userId)) {
            throw new BusinessException("已经点赞过此帖子");
        }

        PostLike postLike = new PostLike();
        postLike.setPost(post);
        postLike.setUser(userRepository.getReferenceById(userId));
        postLikeRepository.save(postLike);

        post.setLikeCount(post.getLikeCount() + 1);
        postRepository.save(post);

        // 发送点赞通知
        if (!post.getAuthor().getId().equals(userId)) {
            notificationService.sendNewPostNotification(post.getAuthor(), post);
        }
    }

    @Override
    @Transactional
    public void unlikePost(Long postId, Long userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException("帖子不存在"));

        PostLike postLike = postLikeRepository.findByPostIdAndUserId(postId, userId)
                .orElseThrow(() -> new BusinessException("未点赞此帖子"));

        postLikeRepository.delete(postLike);
        post.setLikeCount(Math.max(0, post.getLikeCount() - 1));
        postRepository.save(post);
    }

    @Override
    @Transactional
    public PostDTO createPost(PostDTO postDTO, Long userId) {
        User author = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));

        Post post = new Post();
        post.setTitle(postDTO.getTitle());
        post.setContent(postDTO.getContent());
        post.setAuthor(author);
        post.setCreatedAt(LocalDateTime.now());
        post.setStatus(Post.PostStatus.NORMAL);

        if (postDTO.getGameId() != null) {
            Game game = gameRepository.findById(postDTO.getGameId())
                    .orElseThrow(() -> new BusinessException("游戏不存在"));
            post.setGame(game);
        }

        Post savedPost = postRepository.save(post);

        // 通知关注者
        notifyFollowers(savedPost);

        return convertToDTO(savedPost);
    }

    @Override
    @Transactional
    @RequirePermission("post:update")
    @CacheEvict(value = "postCache", key = "#postId")
    public PostDTO updatePost(Long postId, PostDTO postDTO, Long userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException("帖子不存在"));

        if (!post.getAuthor().getId().equals(userId)) {
            throw new BusinessException("无权修改此帖子");
        }

        post.setTitle(postDTO.getTitle());
        post.setContent(postDTO.getContent());
        post.setUpdatedAt(LocalDateTime.now());

        if (postDTO.getGameId() != null &&
                (post.getGame() == null || !post.getGame().getId().equals(postDTO.getGameId()))) {
            Game game = gameRepository.findById(postDTO.getGameId())
                    .orElseThrow(() -> new BusinessException("游戏不存在"));
            post.setGame(game);
        }

        return convertToDTO(postRepository.save(post));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PostDTO> searchPosts(String keyword, Pageable pageable) {
        Page<Post> posts;
        if (keyword != null && !keyword.trim().isEmpty()) {
            posts = postRepository.findByTitleContainingOrContentContaining(
                    keyword.trim(), keyword.trim(), pageable);
        } else {
            posts = postRepository.findAll(pageable);
        }
        return posts.map(this::convertToDTO);
    }

    @Override
    @Transactional
    public CommentDTO addComment(Long postId, CommentDTO commentDTO, Long userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException("帖子不存在"));
        User author = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));

        Comment comment = new Comment();
        comment.setContent(commentDTO.getContent());
        comment.setAuthor(author);
        comment.setPost(post);
        comment.setCreatedAt(LocalDateTime.now());

        Comment savedComment = commentRepository.save(comment);

        // 通知帖子作者
        if (!post.getAuthor().getId().equals(userId)) {
            notificationService.sendNewCommentNotification(post.getAuthor(), post, author.getNickname());
        }

        return convertToCommentDTO(savedComment);
    }

    private void notifyFollowers(Post post) {
        post.getAuthor().getFollowers().forEach(follower ->
                notificationService.sendNewPostNotification(follower, post));
    }

    private PostDTO convertToDTO(Post post) {
        PostDTO dto = new PostDTO();
        BeanUtils.copyProperties(post, dto);
        dto.setAuthor(convertToUserDTO(post.getAuthor()));
        if (post.getGame() != null) {
            dto.setGameId(post.getGame().getId());
            dto.setGameName(post.getGame().getTitle());
        }
        return dto;
    }

    private UserDTO convertToUserDTO(User user) {
        UserDTO dto = new UserDTO();
        BeanUtils.copyProperties(user, dto, "password");
        return dto;
    }
}