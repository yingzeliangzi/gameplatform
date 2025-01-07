package com.gameplatform.service;

import com.gameplatform.model.dto.EventDTO;
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
    EventDTO createEvent(EventDTO eventDTO);
    EventDTO updateEvent(Long eventId, EventDTO eventDTO);
    void deleteEvent(Long eventId);
    EventDTO getEventById(Long id, Long userId);
    Page<EventDTO> searchEvents(String keyword, Event.EventType type, Long userId, Pageable pageable);
    Page<EventDTO> getOngoingEvents(Long userId, Pageable pageable);
    Page<EventDTO> getUpcomingEvents(Long userId, Pageable pageable);
    EventDTO registerForEvent(Long eventId, Long userId, EventDTO registrationDTO);
    void cancelRegistration(Long eventId, Long userId);
    void cancelEvent(Long eventId);

    // 补充缺失的方法
    List<Event> getEventsStartingBetween(LocalDateTime start, LocalDateTime end);
    List<Event> findPendingEvents();
    void updateEventStatus(Long eventId, Event.EventStatus status);
    boolean checkEventCapacity(Long eventId);
    List<EventRegistration> getEventParticipants(Long eventId);
    Event updateEventDetails(Long eventId, Event eventDetails);
    List<Event> getUserEvents(Long userId, Pageable pageable);
    void sendEventReminders(Event event);
    List<Event> findByEndTimeBetween(LocalDateTime start, LocalDateTime end);
    List<Event> findByStatus(Event.EventStatus status);
}