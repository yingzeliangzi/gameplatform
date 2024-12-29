package com.gameplatform.service.impl;
import com.gameplatform.exception.BusinessException;
import com.gameplatform.model.dto.UserDTO;
import com.gameplatform.model.entity.User;
import com.gameplatform.repository.UserRepository;
import com.gameplatform.service.UserService;
import com.gameplatform.util.FileUtil;
import com.gameplatform.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;


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

    @Override
    @Transactional
    public UserDTO register(UserDTO userDTO) {
        // 验证用户名和邮箱是否已存在
        if (userRepository.existsByUsername(userDTO.getUsername())) {
            throw new BusinessException("用户名已存在");
        }
        if (userRepository.existsByEmail(userDTO.getEmail())) {
            throw new BusinessException("邮箱已被注册");
        }

        // 创建新用户
        User user = new User();
        user.setUsername(userDTO.getUsername());
        user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        user.setNickname(userDTO.getNickname());
        user.setEmail(userDTO.getEmail());

        // 设置默认角色
        Set<String> roles = new HashSet<>();
        roles.add("USER");
        user.setRoles(roles);

        User savedUser = userRepository.save(user);

        // 发送验证邮件
        emailService.sendHtmlEmail(
                userDTO.getEmail(),
                "欢迎加入游戏社区",
                createWelcomeEmailContent(userDTO.getUsername())
        );

        return convertToDTO(savedUser);
    }

    // 发送验证码
    @Override
    public void sendVerificationCode(String email) {
        String code = generateVerificationCode(); // 生成6位验证码
        // 存储验证码到Redis或数据库，设置过期时间
        cacheVerificationCode(email, code);

        emailService.sendSimpleEmail(
                email,
                "验证码",
                String.format("您的验证码是：%s，5分钟内有效。", code)
        );
    }

    // 密码重置
    @Override
    public void resetPassword(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException("用户不存在"));

        String newPassword = generateRandomPassword();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        emailService.sendHtmlEmail(
                email,
                "密码重置",
                createPasswordResetEmailContent(newPassword)
        );
    }

    // 创建欢迎邮件内容
    private String createWelcomeEmailContent(String username) {
        return String.format("""
            <div style="padding: 20px; background-color: #f5f7fa;">
                <h1 style="color: #409EFF;">欢迎加入游戏社区！</h1>
                <p>亲爱的 %s：</p>
                <p>感谢您注册成为我们的会员。在这里，您可以：</p>
                <ul>
                    <li>发现精彩游戏</li>
                    <li>参与社区讨论</li>
                    <li>结识志同道合的玩家</li>
                </ul>
                <p>如果您有任何问题，请随时联系我们的客服团队。</p>
                <p>祝您游戏愉快！</p>
            </div>
            """, username);
    }

    // 创建密码重置邮件内容
    private String createPasswordResetEmailContent(String newPassword) {
        return String.format("""
            <div style="padding: 20px; background-color: #f5f7fa;">
                <h1 style="color: #409EFF;">密码重置</h1>
                <p>您的密码已经重置。</p>
                <p>新密码：<strong>%s</strong></p>
                <p>请使用新密码登录后立即修改密码。</p>
                <p>如果这不是您本人的操作，请立即联系我们。</p>
            </div>
            """, newPassword);
    }

    @Override
    public UserDTO login(String username, String password) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException("用户不存在"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BusinessException("密码错误");
        }

        if (user.getStatus() == User.UserStatus.DISABLED) {
            throw new BusinessException("账号已被禁用");
        }

        // 更新最后登录时间
        user.setLastLoginTime(LocalDateTime.now());
        userRepository.save(user);

        UserDTO userDTO = convertToDTO(user);
        userDTO.setToken(jwtUtil.generateToken(username));
        return userDTO;
    }

    @Override
    public void logout(String token) {
        // 如果使用了Redis存储token，这里可以将token加入黑名单
    }

    @Override
    public UserDTO getCurrentUser(String token) {
        String username = jwtUtil.getUsernameFromToken(token);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        return convertToDTO(user);
    }

    @Override
    @Transactional
    public UserDTO updateUser(Long userId, UserDTO userDTO) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));

        // 验证邮箱唯一性
        if (!user.getEmail().equals(userDTO.getEmail()) &&
                userRepository.existsByEmail(userDTO.getEmail())) {
            throw new BusinessException("邮箱已被注册");
        }

        user.setNickname(userDTO.getNickname());
        user.setEmail(userDTO.getEmail());
        user.setBio(userDTO.getBio());

        User updatedUser = userRepository.save(user);
        return convertToDTO(updatedUser);
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
    }

    @Override
    @Transactional
    public String updateAvatar(Long userId, MultipartFile file) {
        if (!fileUtil.isImage(file)) {
            throw new BusinessException("请上传图片文件");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));

        try {
            // 删除旧头像
            if (user.getAvatar() != null) {
                fileUtil.deleteFile(user.getAvatar());
            }

            // 保存新头像
            String avatarPath = fileUtil.saveFile(file, "avatars");
            user.setAvatar(avatarPath);
            userRepository.save(user);

            return fileUtil.getFileUrl(avatarPath);
        } catch (Exception e) {
            throw new BusinessException("头像上传失败");
        }
    }

    @Override
    @Transactional
    public void verifyEmail(Long userId, String code) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));

        // 验证邮箱验证码
        if (!emailService.verifyCode(user.getEmail(), code)) {
            throw new BusinessException("验证码无效或已过期");
        }

        user.setVerified(true);
        userRepository.save(user);
    }

    @Override
    public void sendVerificationCode(String email) {
        emailService.sendVerificationCode(email);
    }

    @Override
    @Transactional
    public void resetPassword(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException("用户不存在"));

        // 生成随机密码
        String newPassword = generateRandomPassword();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // 发送重置密码邮件
        emailService.sendPasswordResetEmail(email, newPassword);
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
    public Page<UserDTO> searchUsers(String keyword, Pageable pageable) {
        return userRepository.searchUsers(keyword, pageable)
                .map(this::convertToDTO);
    }

    @Override
    public Page<UserDTO> getUserFollowers(Long userId, Pageable pageable) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));

        // 这里需要自定义查询方法来实现分页
        // 示例实现可能需要额外的Repository方法
        return null;
    }

    @Override
    public Page<UserDTO> getUserFollowing(Long userId, Pageable pageable) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));

        // 同上，需要自定义查询方法
        return null;
    }

    // DTO转换方法
    private UserDTO convertToDTO(User user) {
        UserDTO dto = new UserDTO();
        BeanUtils.copyProperties(user, dto, "password");
        return dto;
    }

    // 生成随机密码
    private String generateRandomPassword() {
        return UUID.randomUUID().toString().substring(0, 8);
    }



}
