package com.gameplatform.repository;
import com.gameplatform.model.entity.Event;
import com.gameplatform.model.entity.Game;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
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
    Page<Event> findByTitleContainingIgnoreCase(String title, Pageable pageable);
    @Query("SELECT e FROM Event e WHERE e.title LIKE %:keyword%")
    Page<Event> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);
    @Query("SELECT e FROM Event e WHERE " +
            "(:keyword IS NULL OR LOWER(e.title) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND (:type IS NULL OR e.type = :type)")
    Page<Event> findBySearchCriteria(
            @Param("keyword") String keyword,
            @Param("type") Event.EventType type,
            Pageable pageable
    );
    @Query("SELECT e FROM Event e WHERE e.status = :status " +
            "ORDER BY e.currentParticipants DESC, e.createdAt DESC")
    Page<Event> findPopularEvents(@Param("status") Event.EventStatus status, Pageable pageable);

    @Query("SELECT e FROM Event e WHERE e.status = :status AND e.game IN :games " +
            "AND e.startTime > CURRENT_TIMESTAMP " +
            "ORDER BY e.startTime ASC")
    List<Event> findByStatusAndGameIn(
            @Param("status") Event.EventStatus status,
            @Param("games") Collection<Game> games,
            Pageable pageable
    );

    @Query("SELECT COUNT(r) FROM EventRegistration r WHERE r.event.id = :eventId " +
            "AND r.status = 'REGISTERED'")
    int countRegisteredParticipants(@Param("eventId") Long eventId);

    @Query("SELECT COUNT(er) FROM EventRegistration er WHERE er.user.id = :userId")
    long countByUserId(@Param("userId") Long userId);

    long countByStatusAndStartTimeAfter(Event.EventStatus status, LocalDateTime date);

    long countByStatusAndStartTimeBeforeAndEndTimeAfter(
            Event.EventStatus status,
            LocalDateTime currentTime,
            LocalDateTime currentTime2
    );

    Page<Event> findByStartTimeBeforeAndEndTimeAfterAndStatus(
            LocalDateTime startTime,
            LocalDateTime endTime,
            Event.EventStatus status,
            Pageable pageable
    );

    Page<Event> findByStartTimeAfterAndStatus(
            LocalDateTime startTime,
            Event.EventStatus status,
            Pageable pageable
    );
}
