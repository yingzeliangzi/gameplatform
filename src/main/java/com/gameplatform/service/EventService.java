package com.gameplatform.service;

import com.gameplatform.model.dto.EventDTO;
import com.gameplatform.model.dto.EventListItemDTO;
import com.gameplatform.model.dto.EventRegistrationDTO;
import com.gameplatform.model.entity.Event;
import com.gameplatform.model.entity.EventRegistration;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author SakurazawaRyoko
 * @version 1.0
 * @date 2024/12/28 15:12
 * @description TODO
 */
public interface EventService {
    // 基本CRUD操作
    EventDTO createEvent(EventDTO eventDTO);
    EventDTO updateEvent(Long eventId, EventDTO eventDTO);
    void deleteEvent(Long eventId);
    EventDTO getEventById(Long id, Long userId);

    // 查询和搜索
    Page<EventDTO> searchEvents(String keyword, Event.EventType type, Long userId, Pageable pageable);
    Page<EventDTO> getOngoingEvents(Long userId, Pageable pageable);
    Page<EventDTO> getUpcomingEvents(Long userId, Pageable pageable);
    List<Event> getEventsStartingBetween(LocalDateTime start, LocalDateTime end);
    List<Event> findByEndTimeBetween(LocalDateTime start, LocalDateTime end);
    List<Event> findByStatus(Event.EventStatus status);
    List<Event> findPendingEvents();

    // 报名相关
    EventDTO registerForEvent(Long eventId, Long userId, EventDTO registrationDTO);
    void cancelRegistration(Long eventId, Long userId);
    void cancelEvent(Long eventId);
    boolean checkEventCapacity(Long eventId);
    List<EventRegistration> getEventParticipants(Long eventId);
    Page<EventRegistrationDTO> getEventRegistrations(Long eventId, Pageable pageable);
    Page<EventRegistrationDTO> getUserRegistrations(Long userId, Pageable pageable);

    // 事件状态管理
    void updateEventStatus(Long eventId, Event.EventStatus status);
    Event updateEventDetails(Long eventId, Event eventDetails);

    // 用户相关
    List<Event> getUserEvents(Long userId, Pageable pageable);

    // 提醒相关
    void sendEventReminders(Event event);
    void sendRegistrationConfirmation(EventRegistration registration);
    void sendEventCancellationNotice(Long eventId);

    // 统计相关
    long countOngoingEvents();
    long countUpcomingEvents();
    long countEventsByGameId(Long gameId);
    long countEventsByUserId(Long userId);
    double getParticipationRate(Long eventId);

    // 推荐相关
    List<EventDTO> getRecommendedEvents(Long userId, int limit);
    List<EventDTO> getPopularEvents(int limit);
    List<EventDTO> getEventsNearby(Double latitude, Double longitude, Double radiusInKm, int limit);

    // 批量操作
    void batchUpdateEventStatus(List<Long> eventIds, Event.EventStatus status);
    void batchDeleteEvents(List<Long> eventIds);
    void batchCancelRegistrations(Long eventId, List<Long> userIds);
}