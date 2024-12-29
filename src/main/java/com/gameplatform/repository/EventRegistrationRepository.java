package com.gameplatform.repository;
import com.gameplatform.model.entity.EventRegistration;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;


/**
 * @author SakurazawaRyoko
 * @version 1.0
 * @date 2024/12/28 19:34
 * @description TODO
 */
public interface EventRegistrationRepository extends JpaRepository<EventRegistration, Long> {
    Optional<EventRegistration> findByEventIdAndUserId(Long eventId, Long userId);
    List<EventRegistration> findByEventId(Long eventId);
    List<EventRegistration> findByUserId(Long userId);
    boolean existsByEventIdAndUserIdAndStatus(Long eventId, Long userId, EventRegistration.RegistrationStatus status);
    @Query("SELECT COUNT(er) FROM EventRegistration er WHERE er.event.id = :eventId AND er.status = :status")
    long countByEventIdAndStatus(@Param("eventId") Long eventId, @Param("status") EventRegistration.RegistrationStatus status);
}
