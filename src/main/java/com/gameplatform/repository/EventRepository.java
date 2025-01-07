package com.gameplatform.repository;
import com.gameplatform.model.entity.Event;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;


/**
 * @author SakurazawaRyoko
 * @version 1.0
 * @date 2024/12/28 15:11
 * @description TODO
 */
public interface EventRepository extends JpaRepository<Event, Long> {
    Page<Event> findByType(Event.EventType type, Pageable pageable);
    @Query("SELECT e FROM Event e WHERE e.startTime >= :now AND e.status = :status")
    Page<Event> findUpcomingEvents(@Param("now") LocalDateTime now, @Param("status") Event.EventStatus status, Pageable pageable);
    @Query("SELECT e FROM Event e WHERE e.startTime <= :now AND e.endTime >= :now AND e.status = :status")
    Page<Event> findOngoingEvents(@Param("now") LocalDateTime now, @Param("status") Event.EventStatus status, Pageable pageable);
    List<Event> findByStartTimeBetween(LocalDateTime start, LocalDateTime end);
    List<Event> findByEndTimeBetween(LocalDateTime start, LocalDateTime end);
    List<Event> findByStatus(Event.EventStatus status);
    long countByStatus(Event.EventStatus status);
    long countByGameId(Long gameId);
    @Query("SELECT e FROM Event e WHERE e.game.id = :gameId")
    Page<Event> findByGameId(@Param("gameId") Long gameId, Pageable pageable);
}
