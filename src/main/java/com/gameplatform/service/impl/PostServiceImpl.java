package com.gameplatform.service.impl;

import com.gameplatform.exception.BusinessException;
import com.gameplatform.model.dto.CommentDTO;
import com.gameplatform.model.dto.PostDTO;
import com.gameplatform.model.entity.*;
import com.gameplatform.repository.*;
import com.gameplatform.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final NotificationService notificationService;

    @Autowired
    private PostLikeRepository postLikeRepository;

    @Override
    @Transactional(readOnly = true)
    public PostDTO getPostById(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new BusinessException("帖子不存在"));
        post.setViewCount(post.getViewCount() + 1);
        postRepository.save(post);
        return convertToDTO(post);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PostDTO> searchPosts(String keyword, Pageable pageable) {
        Page<Post> posts = keyword != null && !keyword.isEmpty()
                ? postRepository.searchPosts(keyword, pageable)
                : postRepository.findAll(pageable);
        return posts.map(this::convertToDTO);
    }

    @Override
    @Transactional
    public PostDTO createPost(PostDTO postDTO, Long userId) {
        User author = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));

        Post post = new Post();
        BeanUtils.copyProperties(postDTO, post, "id", "author", "game");

        // 设置游戏关联
        if (postDTO.getGameId() != null) {
            Game game = gameRepository.findById(postDTO.getGameId())
                    .orElseThrow(() -> new BusinessException("游戏不存在"));
            post.setGame(game);
        }

        post.setAuthor(author);
        post.setStatus(Post.PostStatus.NORMAL);
        Post savedPost = postRepository.save(post);

        // 如果是游戏相关帖子，通知游戏关注者
        if (savedPost.getGame() != null) {
            notificationService.notifyGameFollowers(savedPost);
        }

        return convertToDTO(savedPost);
    }

    @Override
    @Transactional
    public PostDTO updatePost(Long postId, PostDTO postDTO, Long userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException("帖子不存在"));

        // 验证作者权限
        if (!post.getAuthor().getId().equals(userId)) {
            throw new BusinessException("无权修改此帖子");
        }

        post.setTitle(postDTO.getTitle());
        post.setContent(postDTO.getContent());

        if (postDTO.getGameId() != null && !postDTO.getGameId().equals(post.getGame().getId())) {
            Game game = gameRepository.findById(postDTO.getGameId())
                    .orElseThrow(() -> new BusinessException("游戏不存在"));
            post.setGame(game);
        }

        return convertToDTO(postRepository.save(post));
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

        Comment savedComment = commentRepository.save(comment);

        // 通知帖子作者
        if (!post.getAuthor().getId().equals(userId)) {
            notificationService.notifyPostComment(savedComment);
        }

        return convertToCommentDTO(savedComment);
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

        // 通知被回复的用户
        if (!parentComment.getAuthor().getId().equals(userId)) {
            notificationService.notifyCommentReply(savedReply);
        }

        return convertToCommentDTO(savedReply);
    }

    @Override
    @Transactional
    public void deleteComment(Long commentId, Long userId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new BusinessException("评论不存在"));

        // 验证删除权限（评论作者或帖子作者）
        if (!comment.getAuthor().getId().equals(userId) &&
                !comment.getPost().getAuthor().getId().equals(userId)) {
            throw new BusinessException("无权删除此评论");
        }

        commentRepository.delete(comment);
    }

    @Override
    @Transactional
    public void reportContent(ReportDTO reportDTO, Long userId) {
        User reporter = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));

        Report report = new Report();
        report.setReporter(reporter);
        report.setType(Report.ReportType.valueOf(reportDTO.getType()));
        report.setTargetId(reportDTO.getTargetId());
        report.setReason(reportDTO.getReason());
        report.setStatus(Report.ReportStatus.PENDING);

        reportRepository.save(report);

        // 通知管理员
        notificationService.notifyAdminsNewReport(report);
    }

    @Override
    @Transactional
    public void likePost(Long postId, Long userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException("帖子不存在"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));

        if (postLikeRepository.existsByPostIdAndUserId(postId, userId)) {
            throw new BusinessException("已经点赞过了");
        }

        PostLike like = new PostLike();
        like.setPost(post);
        like.setUser(user);
        postLikeRepository.save(like);

        post.setLikeCount(post.getLikeCount() + 1);
        postRepository.save(post);

        // 通知帖子作者
        if (!post.getAuthor().getId().equals(userId)) {
            notificationService.notifyPostLike(like);
        }
    }

    @Override
    @Transactional
    public void unlikePost(Long postId, Long userId) {
        PostLike like = postLikeRepository.findByPostIdAndUserId(postId, userId)
                .orElseThrow(() -> new BusinessException("未点赞"));

        Post post = like.getPost();
        post.setLikeCount(post.getLikeCount() - 1);
        postRepository.save(post);

        postLikeRepository.delete(like);
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

    private CommentDTO convertToCommentDTO(Comment comment) {
        CommentDTO dto = new CommentDTO();
        BeanUtils.copyProperties(comment, dto);

        dto.setAuthor(convertToUserDTO(comment.getAuthor()));
        if (comment.getParentComment() != null) {
            dto.setParentId(comment.getParentComment().getId());
        }

        return dto;
    }

    private UserDTO convertToUserDTO(User user) {
        UserDTO dto = new UserDTO();
        BeanUtils.copyProperties(user, dto, "password", "roles");
        return dto;
    }
}