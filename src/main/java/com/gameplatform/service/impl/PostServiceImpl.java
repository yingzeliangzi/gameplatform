package com.gameplatform.service.impl;

import com.gameplatform.annotation.RequirePermission;
import com.gameplatform.exception.BusinessException;
import com.gameplatform.model.dto.CommentDTO;
import com.gameplatform.model.dto.PostDTO;
import com.gameplatform.model.entity.Comment;
import com.gameplatform.model.entity.Post;
import com.gameplatform.model.entity.User;
import com.gameplatform.repository.CommentRepository;
import com.gameplatform.repository.PostRepository;
import com.gameplatform.repository.UserRepository;
import com.gameplatform.service.CacheService;
import com.gameplatform.service.NotificationService;
import com.gameplatform.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
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
    private final CommentRepository commentRepository;
    private final NotificationService notificationService;
    private final CacheService cacheService;

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
    @Cacheable(value = "postCache", key = "#postId")
    @Transactional(readOnly = true)
    public PostDTO getPostById(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException("帖子不存在"));

        // 增加浏览量
        post.setViewCount(post.getViewCount() + 1);
        postRepository.save(post);

        PostDTO dto = convertToDTO(post);
        // 加载评论
        dto.setComments(getPostTopComments(postId));
        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PostDTO> searchPosts(String keyword, Pageable pageable) {
        // 确保分页排序
        pageable = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        return postRepository.searchPosts(keyword, pageable)
                .map(this::convertToDTO);
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
            notificationService.sendCommentNotification(post.getAuthor(), post, author);
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
        reply.setCreatedAt(LocalDateTime.now());

        Comment savedReply = commentRepository.save(reply);

        // 通知被回复的用户
        if (!parentComment.getAuthor().getId().equals(userId)) {
            notificationService.sendReplyNotification(parentComment.getAuthor(), parentComment, author);
        }

        return convertToCommentDTO(savedReply);
    }

    @Override
    @Transactional
    @RequirePermission("post:manage")
    public void deletePost(Long postId, Long userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException("帖子不存在"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));

        if (!post.getAuthor().getId().equals(userId) && !user.isAdmin()) {
            throw new BusinessException("无权删除此帖子");
        }

        postRepository.delete(post);
        cacheService.delete("postCache::"+postId);
    }

    private List<CommentDTO> getPostTopComments(Long postId) {
        return commentRepository.findTopLevelCommentsByPostId(postId,
                        PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt")))
                .stream()
                .map(this::convertToCommentDTO)
                .collect(Collectors.toList());
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

    private CommentDTO convertToCommentDTO(Comment comment) {
        CommentDTO dto = new CommentDTO();
        BeanUtils.copyProperties(comment, dto);
        dto.setAuthorId(comment.getAuthor().getId());
        dto.setAuthorName(comment.getAuthor().getNickname());
        dto.setAuthorAvatar(comment.getAuthor().getAvatar());
        if (comment.getParentComment() != null) {
            dto.setParentId(comment.getParentComment().getId());
        }
        return dto;
    }
}