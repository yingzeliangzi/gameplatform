package com.gameplatform.service.impl;

import com.gameplatform.exception.BusinessException;
import com.gameplatform.model.dto.LoginResponseDTO;
import com.gameplatform.model.dto.RegisterRequestDTO;
import com.gameplatform.model.dto.UserDTO;
import com.gameplatform.model.entity.User;
import com.gameplatform.repository.UserRepository;
import com.gameplatform.service.CacheService;
import com.gameplatform.service.EmailService;
import com.gameplatform.service.UserService;
import com.gameplatform.util.FileUtil;
import com.gameplatform.util.JwtUtil;
import com.gameplatform.util.PageUtils;
import com.gameplatform.util.VerificationCodeUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.concurrent.TimeUnit;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author SakurazawaRyoko
 * @version 1.0
 * @date 2024/12/29 1:05
 * @description TODO
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final FileUtil fileUtil;
    private final EmailService emailService;
    private final CacheService cacheService;
    private final VerificationCodeUtil verificationCodeUtil;

    @Override
    @Transactional
    public UserDTO register(RegisterRequestDTO registerRequest) {
        // 验证用户名和邮箱是否已存在
        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            throw new BusinessException("用户名已存在");
        }
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new BusinessException("邮箱已被注册");
        }

        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setEmail(registerRequest.getEmail());
        user.setNickname(registerRequest.getNickname());
        user.setStatus(User.UserStatus.ACTIVE);
        user.setCreatedAt(LocalDateTime.now());
        user.setRoles(new HashSet<>(Collections.singletonList("USER")));

        User savedUser = userRepository.save(user);
        emailService.sendWelcomeEmail(user.getEmail(), user.getUsername());

        return convertToDTO(savedUser);
    }

    @Override
    @Transactional
    public LoginResponseDTO login(String username, String password) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException("用户名或密码错误"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }

        if (user.getStatus() == User.UserStatus.DISABLED) {
            throw new BusinessException("账号已被禁用");
        }

        user.setLastLoginTime(LocalDateTime.now());
        userRepository.save(user);

        UserDTO userDTO = convertToDTO(user);
        String token = jwtUtil.generateToken(username);

        LoginResponseDTO response = new LoginResponseDTO();
        response.setToken(token);
        response.setUser(userDTO);
        return response;
    }

    @Override
    @Transactional
    public void logout(String token) {
        // 可以在这里实现token黑名单等逻辑
        String username = jwtUtil.getUsernameFromToken(token);
        if (username != null) {
            cacheService.delete("userToken:" + username);
        }
    }

    @Override
    @Transactional
    public UserDTO updateUser(Long userId, UserDTO userDTO) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));

        if (userDTO.getNickname() != null) {
            user.setNickname(userDTO.getNickname());
        }
        if (userDTO.getBio() != null) {
            user.setBio(userDTO.getBio());
        }
        if (userDTO.getPhone() != null) {
            user.setPhone(userDTO.getPhone());
        }

        User updatedUser = userRepository.save(user);
        cacheService.invalidateUser(userId);
        return convertToDTO(updatedUser);
    }

    @Override
    @Transactional
    public void sendVerificationCode(String email) {
        String code = verificationCodeUtil.generateCode();
        cacheService.setCache("verificationCode:" + email, code, 15, TimeUnit.MINUTES);
        emailService.sendVerificationEmail(email, code);
    }

    @Override
    @Transactional
    public void resetPassword(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException("邮箱不存在"));

        String newPassword = UUID.randomUUID().toString().substring(0, 8);
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        emailService.sendPasswordResetEmail(email, newPassword);
    }

    @Override
    @Transactional
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));

        user.setStatus(User.UserStatus.DELETED);
        userRepository.save(user);
        cacheService.invalidateUser(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDTO getCurrentUser(String token) {
        String username = jwtUtil.getUsernameFromToken(token);
        return loadUserByUsername(username);
    }

    @Override
    @Transactional
    public void blockUser(Long userId, Long targetUserId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        User targetUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> new BusinessException("目标用户不存在"));

        if (userId.equals(targetUserId)) {
            throw new BusinessException("不能屏蔽自己");
        }

        user.getBlockedUsers().add(targetUser);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void unblockUser(Long userId, Long targetUserId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));

        user.getBlockedUsers().removeIf(blockedUser -> blockedUser.getId().equals(targetUserId));
        userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isUserBlocked(Long userId, Long targetUserId) {
        return userRepository.isUserBlocked(userId, targetUserId);
    }

    private UserDTO convertToDTO(User user) {
        UserDTO dto = new UserDTO();
        BeanUtils.copyProperties(user, dto, "password", "blockedUsers");

        dto.setFollowerCount(user.getFollowers().size());
        dto.setFollowingCount(user.getFollowing().size());
        dto.setGameCount(user.getGames().size());
        dto.setPostCount(user.getPosts().size());

        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> getActiveUsers() {
        return userRepository.findByStatus(User.UserStatus.ACTIVE);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserDTO> getUserFollowers(Long userId, Pageable pageable) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));

        List<User> followers = new ArrayList<>(user.getFollowers());
        return PageUtils.toPage(
                followers.stream()
                        .map(this::convertToDTO)
                        .collect(Collectors.toList()),
                pageable
        );
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserDTO> getUserFollowing(Long userId, Pageable pageable) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));

        List<User> following = new ArrayList<>(user.getFollowing());
        return PageUtils.toPage(
                following.stream()
                        .map(this::convertToDTO)
                        .collect(Collectors.toList()),
                pageable
        );
    }

    @Override
    public boolean validateToken(String token) {
        return jwtUtil.validateToken(token);
    }

    @Override
    @Cacheable(value = "userCache", key = "#username", unless = "#result == null")
    public UserDTO loadUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        return convertToDTO(user);
    }

    @Override
    @CacheEvict(value = "userCache", key = "#userId")
    @Transactional
    public void updatePassword(Long userId, String oldPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BusinessException("原密码错误");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // 发送密码修改通知邮件
        emailService.sendPasswordChangeNotification(user.getEmail());
    }

    @Override
    @Transactional
    public String updateAvatar(Long userId, MultipartFile file) {
        if (!fileUtil.isValidFileType(file.getContentType())) {
            throw new BusinessException("不支持的文件类型");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));

        try {
            String avatarPath = fileUtil.saveFile(file, "avatars");

            // 删除旧头像
            if (user.getAvatar() != null) {
                fileUtil.deleteFile(user.getAvatar());
            }

            user.setAvatar(avatarPath);
            userRepository.save(user);

            // 清除缓存
            cacheService.invalidateUser(userId);

            return fileUtil.getFileUrl(avatarPath);
        } catch (Exception e) {
            log.error("上传头像失败", e);
            throw new BusinessException("上传头像失败");
        }
    }

    @Override
    @Transactional
    public void followUser(Long userId, Long targetUserId) {
        if (userId.equals(targetUserId)) {
            throw new BusinessException("不能关注自己");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        User targetUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> new BusinessException("目标用户不存在"));

        if (user.getFollowing().contains(targetUser)) {
            throw new BusinessException("已经关注过该用户");
        }

        user.getFollowing().add(targetUser);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void unfollowUser(Long userId, Long targetUserId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        User targetUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> new BusinessException("目标用户不存在"));

        if (!user.getFollowing().contains(targetUser)) {
            throw new BusinessException("未关注该用户");
        }

        user.getFollowing().remove(targetUser);
        userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserDTO> searchUsers(String keyword, Pageable pageable) {
        return userRepository.searchUsers(keyword, pageable)
                .map(this::convertToDTO);
    }

    @Override
    @Transactional
    public void enableUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        user.setStatus(User.UserStatus.ACTIVE);
        userRepository.save(user);
        cacheService.invalidateUser(userId);
    }

    @Override
    @Transactional
    public void disableUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        user.setStatus(User.UserStatus.DISABLED);
        userRepository.save(user);
        cacheService.invalidateUser(userId);
    }

    @Override
    @Transactional
    public void updateUserRoles(Long userId, Set<String> roles) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        user.setRoles(roles);
        userRepository.save(user);
        cacheService.invalidateUser(userId);
    }

    @Override
    @Transactional
    public void verifyEmail(Long userId, String code) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));

        String storedCode = (String) cacheService.get("verificationCode:" + user.getEmail())
                .orElseThrow(() -> new BusinessException("验证码已过期"));

        if (!code.equals(storedCode)) {
            throw new BusinessException("验证码错误");
        }

        user.setVerified(true);
        userRepository.save(user);
        cacheService.delete("verificationCode:" + user.getEmail());
    }

    @Override
    @Transactional(readOnly = true)
    public UserDTO getUserProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        return convertToDTO(user);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean validateToken(String token) {
        try {
            return jwtUtil.validateToken(token);
        } catch (Exception e) {
            return false;
        }
    }
}