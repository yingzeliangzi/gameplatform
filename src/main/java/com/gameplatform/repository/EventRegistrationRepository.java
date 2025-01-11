package com.gameplatform.repository;

import com.gameplatform.model.entity.EventRegistration;
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
 * @date 2024/12/28 19:34
 * @description TODO
 */
public interface EventRegistrationRepository extends JpaRepository<EventRegistration, Long> {

    boolean existsByEventIdAndUserIdAndStatus(
            Long eventId,
            Long userId,
            EventRegistration.RegistrationStatus status
    );

    Optional<EventRegistration> findByEventIdAndUserId(Long eventId, Long userId);

    Page<EventRegistration> findByEventId(Long eventId, Pageable pageable);
    Page<EventRegistration> findByUserId(Long userId, Pageable pageable);

    List<EventRegistration> findByEventIdAndStatus(
            Long eventId,
            EventRegistration.RegistrationStatus status
    );

    @Query("SELECT COUNT(er) FROM EventRegistration er WHERE er.registeredAt BETWEEN :start AND :end")
    long countByRegisteredAtBetween(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @Query("SELECT COUNT(er) FROM EventRegistration er WHERE er.user.id = :userId")
    long countByUserId(@Param("userId") Long userId);

    List<EventRegistration> findByUserIdOrderByRegisteredAtDesc(Long userId, Pageable pageable);

    Page<EventRegistration> findByEventIdOrderByRegisteredAtDesc(Long eventId, Pageable pageable);
}