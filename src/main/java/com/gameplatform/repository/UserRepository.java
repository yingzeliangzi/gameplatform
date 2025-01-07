package com.gameplatform.repository;
import com.gameplatform.model.entity.Game;
import com.gameplatform.model.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


/**
 * @author SakurazawaRyoko
 * @version 1.0
 * @date 2024/12/28 15:10
 * @description TODO
 */
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    Optional<User> findByPhone(String phone);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByPhone(String phone);

    long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
    long countByLastLoginTimeAfter(LocalDateTime time);
    List<User> findByOwnedGamesContaining(Game game);

    @Query("SELECT u FROM User u WHERE " +
            "LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(u.nickname) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<User> searchUsers(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT u FROM User u WHERE u.status = :status")
    Page<User> findByStatus(@Param("status") User.UserStatus status, Pageable pageable);

    @Query("SELECT u FROM User u JOIN u.roles r WHERE r = :role")
    List<User> findByRole(@Param("role") String role);

    @Query("SELECT u FROM User u JOIN u.games g WHERE g.game.id = :gameId")
    List<User> findByOwnedGamesContaining(@Param("gameId") Long gameId);

    @Query("SELECT COUNT(u) FROM User u WHERE u.status = :status")
    long countByStatus(@Param("status") User.UserStatus status);

    @Query(value = "SELECT * FROM users u " +
            "WHERE u.status = 'ACTIVE' " +
            "ORDER BY (SELECT COUNT(*) FROM posts p WHERE p.author_id = u.id) DESC " +
            "LIMIT :limit", nativeQuery = true)
    List<User> findTopContributors(@Param("limit") int limit);

    List<User> findByStatus(User.UserStatus status);

    @Query("SELECT u FROM User u WHERE u.id IN " +
            "(SELECT b.id FROM User current JOIN current.blockedUsers b WHERE current.id = :userId)")
    Page<User> findBlockedUsers(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT COUNT(b) > 0 FROM User u JOIN u.blockedUsers b " +
            "WHERE u.id = :userId AND b.id = :targetUserId")
    boolean isUserBlocked(@Param("userId") Long userId, @Param("targetUserId") Long targetUserId);

    long countDailyActiveUsers(LocalDateTime start, LocalDateTime end);
}
