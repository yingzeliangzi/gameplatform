package com.gameplatform.repository;

import com.gameplatform.model.entity.UserSetting;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * @author SakurazawaRyoko
 * @version 1.0
 * @date 2024/12/29 13:45
 * @description TODO
 */
public interface UserSettingRepository extends JpaRepository<UserSetting, Long> {
    List<UserSetting> findByUserId(Long userId);
    Optional<UserSetting> findByUserIdAndKey(Long userId, String key);
    boolean existsByUserIdAndKey(Long userId, String key);
}
