package com.gameplatform.service.impl;

import com.gameplatform.exception.BusinessException;
import com.gameplatform.model.dto.LoginResponseDTO;
import com.gameplatform.model.dto.RegisterRequestDTO;
import com.gameplatform.model.dto.UserDTO;
import com.gameplatform.model.entity.User;
import com.gameplatform.repository.UserGameRepository;
import com.gameplatform.repository.UserRepository;
import com.gameplatform.repository.UserSettingRepository;
import com.gameplatform.service.CacheService;
import com.gameplatform.service.EmailService;
import com.gameplatform.service.UserService;
import com.gameplatform.util.FileUtil;
import com.gameplatform.util.JwtUtil;
import com.gameplatform.util.VerificationCodeUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
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
    private final UserGameRepository userGameRepository;
    private final UserSettingRepository userSettingRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final FileUtil fileUtil;
    private final EmailService emailService;
    private final CacheService cacheService;
    private final VerificationCodeUtil verificationCodeUtil;

    private static final String VERIFICATION_CODE_PREFIX = "verificationCode:";
    private static final String RESET_TOKEN_PREFIX = "resetToken:";
    private static final long VERIFICATION_CODE_EXPIRE = 15; // 15分钟
    private static final int MAX_LOGIN_ATTEMPTS = 5;
    private static final long LOGIN_ATTEMPT_EXPIRE = 30; // 30分钟

    @Override
    @Transactional(readOnly = true)
    public Page<UserDTO> searchUsers(String keyword, Pageable pageable) {
        // 使用repository的查询方法
        Page<User> users;
        if (keyword != null && !keyword.trim().isEmpty()) {
            users = userRepository.searchUsers(keyword.trim(), pageable);
        } else {
            users = userRepository.findAll(pageable);
        }

        // 转换为DTO并返回
        return users.map(this::convertToDTO);
    }

    @Override
    @Transactional
    public void disableUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));

        if (user.isAdmin()) {
            throw new BusinessException("不能禁用管理员账户");
        }

        user.setStatus(User.UserStatus.DISABLED);
        userRepository.save(user);

        // 清除用户缓存和相关会话
        cacheService.invalidateUser(userId);
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

    // 修复Long类型的拆箱问题
    public UserDTO getUserProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));

        UserDTO userDTO = convertToDTO(user);
        Long gameCount = userGameRepository.countByUserId(userId);
        userDTO.setGameCount(gameCount != null ? gameCount.intValue() : 0);

        return userDTO;
    }

    // 修复缓存相关的方法
    private void setCacheData(String key, Object value, long timeout, TimeUnit unit) {
        Map<String, Object> cacheData = new HashMap<>();
        cacheData.put("value", value);
        cacheData.put("timestamp", System.currentTimeMillis());
        cacheService.setCache(key, cacheData, timeout, unit);
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> getActiveUsers() {
        return userRepository.findByStatus(User.UserStatus.ACTIVE);
    }

    @Override
    @Transactional
    public void updateUserRoles(Long userId, Set<String> roles) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        user.setRoles(roles);
        userRepository.save(user);

        // 清除用户缓存
        cacheService.invalidateUser(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserDTO> getUserFollowers(Long userId, Pageable pageable) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));

        // 处理分页
        List<User> followers = new ArrayList<>(user.getFollowers());
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), followers.size());
        List<User> pageContent = followers.subList(start, end);

        // 转换为DTO并返回分页结果
        List<UserDTO> dtoList = pageContent.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return new PageImpl<>(dtoList, pageable, followers.size());
    }

    @Override
    @Transactional
    public LoginResponseDTO login(String username, String password) {
        String loginAttemptKey = "loginAttempt:" + username;

        // 检查登录尝试次数
        Integer attempts = (Integer) cacheService.get(loginAttemptKey)
                .orElse(0);

        if (attempts >= MAX_LOGIN_ATTEMPTS) {
            throw new BusinessException("账号已被锁定，请" + LOGIN_ATTEMPT_EXPIRE + "分钟后重试");
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    incrementLoginAttempts(loginAttemptKey, attempts);
                    return new BusinessException("用户名或密码错误");
                });

        if (!passwordEncoder.matches(password, user.getPassword())) {
            incrementLoginAttempts(loginAttemptKey, attempts);
            throw new BusinessException("用户名或密码错误");
        }

        // 清除登录尝试记录
        cacheService.delete(loginAttemptKey);

        // 更新最后登录时间
        user.setLastLoginTime(LocalDateTime.now());
        userRepository.save(user);

        // 生成token
        String token = jwtUtil.generateToken(username);
        // 存储token
        setCacheData("userToken:" + username, token, 24, TimeUnit.HOURS);

        // 构建响应
        LoginResponseDTO response = new LoginResponseDTO();
        response.setToken(token);
        response.setUser(convertToDTO(user));
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserDTO> getUserFollowing(Long userId, Pageable pageable) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));

        // 创建一个新的 ArrayList 来存储关注的用户
        List<User> following = new ArrayList<>(user.getFollowing());

        // 手动实现分页
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), following.size());
        List<User> pageContent = following.subList(start, end);

        // 转换为 DTO
        List<UserDTO> dtoList = pageContent.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return new PageImpl<>(dtoList, pageable, following.size());
    }

    @Override
    @Transactional
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));

        // 清除用户相关数据
        user.setStatus(User.UserStatus.DELETED);
        userRepository.save(user);

        // 清除用户缓存
        cacheService.invalidateUser(userId);
    }

    @Override
    public String refreshToken(String token) {
        if (!jwtUtil.validateToken(token)) {
            throw new BusinessException("无效的token");
        }

        String username = jwtUtil.getUsernameFromToken(token);
        UserDTO user = loadUserByUsername(username);

        if (user != null) {
            return jwtUtil.generateToken(username);
        }

        throw new BusinessException("刷新token失败");
    }

    @Override
    @Transactional
    public UserDTO register(RegisterRequestDTO registerRequest) {
        // 参数验证
        validateRegistrationRequest(registerRequest);

        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setEmail(registerRequest.getEmail());
        user.setNickname(registerRequest.getNickname());
        user.setStatus(User.UserStatus.ACTIVE);
        user.setCreatedAt(LocalDateTime.now());
        user.setRoles(new HashSet<>(Collections.singletonList("USER")));
        user.setVerified(false);

        User savedUser = userRepository.save(user);

        // 发送验证邮件
        String verificationCode = verificationCodeUtil.generateCode();
        cacheService.setCache(
                VERIFICATION_CODE_PREFIX + user.getEmail(),
                verificationCode,
                VERIFICATION_CODE_EXPIRE,
                TimeUnit.MINUTES
        );
        emailService.sendVerificationEmail(user.getEmail(), verificationCode);

        return convertToDTO(savedUser);
    }

    private void incrementLoginAttempts(String key, Integer attempts) {
        cacheService.setCache(key, attempts + 1, LOGIN_ATTEMPT_EXPIRE, TimeUnit.MINUTES);
    }

    @Override
    @Transactional
    public void sendVerificationCode(String email) {
        if (!userRepository.existsByEmail(email)) {
            throw new BusinessException("邮箱不存在");
        }

        String code = verificationCodeUtil.generateCode();
        cacheService.setCache(VERIFICATION_CODE_PREFIX + email, code, VERIFICATION_CODE_EXPIRE, TimeUnit.MINUTES);
        emailService.sendVerificationEmail(email, code);
    }

    @Override
    @Transactional
    public void verifyEmail(Long userId, String code) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));

        String cacheKey = VERIFICATION_CODE_PREFIX + user.getEmail();
        String storedCode = (String) cacheService.get(cacheKey)
                .orElseThrow(() -> new BusinessException("验证码已过期"));

        if (!verificationCodeUtil.verifyCode(storedCode, code)) {
            throw new BusinessException("验证码错误");
        }

        user.setVerified(true);
        userRepository.save(user);
        cacheService.delete(cacheKey);
    }

    @Override
    @Transactional
    public void resetPassword(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException("邮箱不存在"));

        String resetToken = UUID.randomUUID().toString();
        cacheService.setCache(RESET_TOKEN_PREFIX + email, resetToken, 24, TimeUnit.HOURS);

        // 生成临时密码
        String tempPassword = generateSecurePassword();
        user.setPassword(passwordEncoder.encode(tempPassword));
        userRepository.save(user);

        emailService.sendPasswordResetEmail(email, tempPassword);
    }

    private String generateSecurePassword() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    @Override
    @Transactional
    public void updatePassword(Long userId, String oldPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BusinessException("原密码错误");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        emailService.sendPasswordChangeNotification(user.getEmail());

        // 使所有token失效
        cacheService.delete("userToken:" + user.getUsername());
    }

    @Override
    @Transactional
    public void logout(String token) {
        String username = jwtUtil.getUsernameFromToken(token);
        if (username != null) {
            cacheService.delete("userToken:" + username);
        }
    }

    @Override
    @Transactional
    @CacheEvict(value = "userCache", key = "#userId")
    public UserDTO updateUser(Long userId, UserDTO userDTO) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));

        if (userDTO.getNickname() != null) {
            user.setNickname(userDTO.getNickname());
        }
        if (userDTO.getBio() != null) {
            user.setBio(userDTO.getBio());
        }
        if (userDTO.getPhone() != null && !user.getPhone().equals(userDTO.getPhone())) {
            validatePhone(userDTO.getPhone());
            user.setPhone(userDTO.getPhone());
        }

        User updatedUser = userRepository.save(user);
        return convertToDTO(updatedUser);
    }

    @Override
    @Transactional
    public String updateAvatar(Long userId, MultipartFile file) {
        validateFileType(file);
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
            cacheService.invalidateUser(userId);

            return fileUtil.getFileUrl(avatarPath);
        } catch (IOException e) {
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

        if (isUserBlocked(targetUserId, userId)) {
            throw new BusinessException("无法关注该用户");
        }

        if (user.getFollowing().contains(targetUser)) {
            throw new BusinessException("已经关注过该用户");
        }

        user.follow(targetUser);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void unfollowUser(Long userId, Long targetUserId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        User targetUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> new BusinessException("目标用户不存在"));

        user.unfollow(targetUser);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void blockUser(Long userId, Long targetUserId) {
        if (userId.equals(targetUserId)) {
            throw new BusinessException("不能屏蔽自己");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        User targetUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> new BusinessException("目标用户不存在"));

        user.getBlockedUsers().add(targetUser);

        // 如果被屏蔽用户已关注该用户，则自动取消关注
        if (targetUser.getFollowing().contains(user)) {
            targetUser.unfollow(user);
            userRepository.save(targetUser);
        }

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

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "userCache", key = "#username", unless = "#result == null")
    public UserDTO loadUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        return convertToDTO(user);
    }

    private void validateRegistrationRequest(RegisterRequestDTO request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException("用户名已存在");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("邮箱已被注册");
        }

        // 其他验证逻辑...
    }

    private void validateFileType(MultipartFile file) {
        if (!fileUtil.isValidFileType(file)) {
            throw new BusinessException("不支持的文件类型");
        }
    }

    private void validatePhone(String phone) {
        if (userRepository.existsByPhone(phone)) {
            throw new BusinessException("手机号已被使用");
        }
        // 手机号格式验证...
    }

    private UserDTO convertToDTO(User user) {
        UserDTO dto = new UserDTO();
        BeanUtils.copyProperties(user, dto, "password", "blockedUsers");

        if (user.getAvatar() != null) {
            dto.setAvatar(fileUtil.getFileUrl(user.getAvatar()));
        }

        dto.setFollowerCount(user.getFollowers().size());
        dto.setFollowingCount(user.getFollowing().size());
        dto.setGameCount(user.getGames().size());
        dto.setPostCount(user.getPosts().size());

        return dto;
    }

    // 实现接口中的其他方法...
    @Override
    public UserDTO getCurrentUser(String token) {
        String username = jwtUtil.getUsernameFromToken(token);
        return loadUserByUsername(username);
    }

    @Override
    public boolean validateToken(String token) {
        return jwtUtil.validateToken(token);
    }
}