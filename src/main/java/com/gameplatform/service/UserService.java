package com.gameplatform.service;
import com.gameplatform.model.dto.LoginResponseDTO;
import com.gameplatform.model.dto.RegisterRequestDTO;
import com.gameplatform.model.dto.UserDTO;
import com.gameplatform.model.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;


/**
 * @author SakurazawaRyoko
 * @version 1.0
 * @date 2024/12/28 15:11
 * @description TODO
 */
public interface UserService {
    UserDTO register(RegisterRequestDTO registerRequest);
    void logout(String token);
    UserDTO updateUser(Long userId, UserDTO userDTO);
    void updatePassword(Long userId, String oldPassword, String newPassword);
    String updateAvatar(Long userId, MultipartFile file);
    void verifyEmail(Long userId, String code);
    void followUser(Long userId, Long targetUserId);
    void unfollowUser(Long userId, Long targetUserId);
    Page<UserDTO> searchUsers(String keyword, Pageable pageable);
    void disableUser(Long userId);
    void enableUser(Long userId);
    UserDTO getUserProfile(Long userId);
    UserDTO loadUserByUsername(String username);
    List<User> getActiveUsers();
    void updateUserRoles(Long userId, Set<String> roles);
    Page<UserDTO> getUserFollowers(Long userId, Pageable pageable);
    Page<UserDTO> getUserFollowing(Long userId, Pageable pageable);
    boolean validateToken(String token);
    void blockUser(Long userId, Long targetUserId);
    void unblockUser(Long userId, Long targetUserId);
    LoginResponseDTO login(String username, String password);
    void deleteUser(Long userId);
    UserDTO getCurrentUser(String token);
    void sendVerificationCode(String email);
    void resetPassword(String email);
    boolean isUserBlocked(Long userId, Long targetUserId);
    String refreshToken(String token);
}
