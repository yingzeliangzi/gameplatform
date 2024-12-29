package com.gameplatform.service;
import com.gameplatform.model.dto.EventDTO;
import com.gameplatform.model.dto.EventListItemDTO;
import com.gameplatform.model.dto.EventRegistrationDTO;
import com.gameplatform.model.entity.Event;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


/**
 * @author SakurazawaRyoko
 * @version 1.0
 * @date 2024/12/28 15:12
 * @description TODO
 */
public interface EventService {
    EventDTO createEvent(EventDTO eventDTO);

    EventDTO updateEvent(Long eventId, EventDTO eventDTO);

    void cancelEvent(Long eventId);

    EventDTO getEventById(Long eventId, Long userId);

    Page<EventListItemDTO> getUpcomingEvents(Long userId, Pageable pageable);

    Page<EventListItemDTO> getOngoingEvents(Long userId, Pageable pageable);

    Page<EventListItemDTO> searchEvents(String keyword, Event.EventType type, Long userId, Pageable pageable);

    Page<EventListItemDTO> getEventsByGame(Long gameId, Long userId, Pageable pageable);

    EventRegistrationDTO registerForEvent(Long eventId, Long userId, EventRegistrationDTO registrationDTO);

    void cancelRegistration(Long eventId, Long userId);

    Page<EventRegistrationDTO> getUserRegistrations(Long userId, Pageable pageable);

    Page<EventRegistrationDTO> getEventRegistrations(Long eventId, Pageable pageable);

    void updateRegistrationStatus(Long registrationId, EventRegistration.RegistrationStatus status);
}
