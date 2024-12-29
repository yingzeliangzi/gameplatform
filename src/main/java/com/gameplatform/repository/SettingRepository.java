package com.gameplatform.repository;

import com.gameplatform.model.entity.Setting;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * @author SakurazawaRyoko
 * @version 1.0
 * @date 2024/12/29 13:45
 * @description TODO
 */
public interface SettingRepository extends JpaRepository<Setting, Long> {
    Optional<Setting> findByKey(String key);
    boolean existsByKey(String key);
}