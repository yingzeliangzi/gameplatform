package com.gameplatform.service;

import com.gameplatform.model.dto.EventDTO;
import com.gameplatform.model.dto.EventListItemDTO;
import com.gameplatform.model.dto.EventRegistrationDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * @author SakurazawaRyoko
 * @version 1.0
 * @date 2025/01/06 2:17
 * @description TODO
 */

public interface BaseEventService {
    EventDTO createEvent(EventDTO eventDTO);
    EventDTO updateEvent(Long eventId, EventDTO eventDTO);
    void deleteEvent(Long eventId);
    Page<EventListItemDTO> getOngoingEvents(Long userId, Pageable pageable);
    Page<EventListItemDTO> getUpcomingEvents(Long userId, Pageable pageable);
    EventRegistrationDTO registerForEvent(Long eventId, Long userId, EventRegistrationDTO registrationDTO);
    void cancelRegistration(Long eventId, Long userId);
}