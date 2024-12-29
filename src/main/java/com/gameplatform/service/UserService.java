package com.gameplatform.service;
import com.gameplatform.model.dto.UserDTO;
import com.gameplatform.model.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;


/**
 * @author SakurazawaRyoko
 * @version 1.0
 * @date 2024/12/28 15:11
 * @description TODO
 */
public interface UserService {
    UserDTO register(UserDTO userDTO);

    UserDTO login(String username, String password);

    void logout(String token);

    UserDTO getCurrentUser(String token);

    UserDTO updateUser(Long userId, UserDTO userDTO);

    void updatePassword(Long userId, String oldPassword, String newPassword);

    String updateAvatar(Long userId, MultipartFile file);

    void verifyEmail(Long userId, String code);

    void sendVerificationCode(String email);

    void resetPassword(String email);

    void enableTwoFactor(Long userId);

    void disableTwoFactor(Long userId);

    void followUser(Long userId, Long targetUserId);

    void unfollowUser(Long userId, Long targetUserId);

    Page<UserDTO> searchUsers(String keyword, Pageable pageable);

    Page<UserDTO> getUserFollowers(Long userId, Pageable pageable);

    Page<UserDTO> getUserFollowing(Long userId, Pageable pageable);

    void blockUser(Long userId, Long targetUserId);

    void unblockUser(Long userId, Long targetUserId);

    Page<UserDTO> getBlockedUsers(Long userId, Pageable pageable);

    boolean isUserBlocked(Long userId, Long targetUserId);

    void deleteUser(Long userId);

    void disableUser(Long userId);

    void enableUser(Long userId);

    UserDTO getUserProfile(Long userId);

    boolean validateToken(String token);

    UserDTO loadUserByUsername(String username);
}
