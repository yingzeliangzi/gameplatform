package com.gameplatform.model.entity;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author SakurazawaRyoko
 * @version 1.0
 * @date 2024/12/28 15:10
 * @description TODO
 */
@Data
@Entity
@Table(name = "users")
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String nickname;

    @Column(nullable = false, unique = true)
    private String email;

    private String avatar;

    @Column(length = 500)
    private String bio;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "role")
    private Set<String> roles = new HashSet<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private Set<UserGame> games = new HashSet<>();

    @OneToMany(mappedBy = "author", cascade = CascadeType.ALL)
    private Set<Post> posts = new HashSet<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private Set<EventRegistration> eventRegistrations = new HashSet<>();

    @ManyToMany
    @JoinTable(
            name = "user_followers",
            joinColumns = @JoinColumn(name = "followed_id"),
            inverseJoinColumns = @JoinColumn(name = "follower_id")
    )
    private Set<User> followers = new HashSet<>();

    @ManyToMany(mappedBy = "followers")
    private Set<User> following = new HashSet<>();

    @ManyToMany
    @JoinTable(
            name = "user_blocks",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "blocked_user_id")
    )
    private Set<User> blockedUsers = new HashSet<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private Set<Notification> notifications = new HashSet<>();

    private boolean isVerified = false;
    private String phone;
    private boolean twoFactorEnabled = false;
    private String twoFactorSecret;

    @Enumerated(EnumType.STRING)
    private UserStatus status = UserStatus.ACTIVE;

    private LocalDateTime lastLoginTime;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Version
    private Long version;

    public enum UserStatus {
        ACTIVE,
        DISABLED,
        DELETED
    }

    // 用户设置相关方法
    public UserSetting getSetting(String key) {
        // 在实际应用中需要注入UserSettingRepository来实现
        return null;
    }

    // UserDetails接口实现
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .collect(Collectors.toList());
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return status != UserStatus.DISABLED;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return status == UserStatus.ACTIVE;
    }

    // 角色和权限相关方法
    public boolean hasRole(String role) {
        return roles.contains(role);
    }

    public void addRole(String role) {
        roles.add(role);
    }

    public void removeRole(String role) {
        roles.remove(role);
    }

    public boolean isAdmin() {
        return hasRole("ADMIN");
    }

    // 关注相关方法
    public void follow(User user) {
        following.add(user);
        user.getFollowers().add(this);
    }

    public void unfollow(User user) {
        following.remove(user);
        user.getFollowers().remove(this);
    }

    public boolean isFollowing(User user) {
        return following.contains(user);
    }

    // 封禁相关方法
    public void blockUser(User user) {
        blockedUsers.add(user);
        if (this.isFollowing(user)) {
            this.unfollow(user);
        }
        if (user.isFollowing(this)) {
            user.unfollow(this);
        }
    }

    public void unblockUser(User user) {
        blockedUsers.remove(user);
    }

    public boolean hasBlockedUser(User user) {
        return blockedUsers.contains(user);
    }

    // 游戏相关方法
    public boolean hasGame(Game game) {
        return games.stream()
                .anyMatch(userGame -> userGame.getGame().equals(game));
    }

    // 通知相关方法
    public boolean hasUnreadNotifications() {
        return notifications.stream()
                .anyMatch(notification -> !notification.isRead());
    }

    public long getUnreadNotificationCount() {
        return notifications.stream()
                .filter(notification -> !notification.isRead())
                .count();
    }

    public void markAllNotificationsAsRead() {
        notifications.forEach(Notification::markAsRead);
    }
}