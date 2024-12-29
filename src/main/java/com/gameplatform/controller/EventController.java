package com.gameplatform.controller;
import com.gameplatform.model.dto.EventDTO;
import com.gameplatform.model.dto.EventListItemDTO;
import com.gameplatform.model.dto.EventRegistrationDTO;
import com.gameplatform.model.entity.Event;
import com.gameplatform.service.EventService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;


/**
 * @author SakurazawaRyoko
 * @version 1.0
 * @date 2024/12/28 15:09
 * @description TODO
 */
@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventController {
    private final EventService eventService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EventDTO> createEvent(@Valid @RequestBody EventDTO eventDTO) {
        return ResponseEntity.ok(eventService.createEvent(eventDTO));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EventDTO> updateEvent(
            @PathVariable Long id,
            @Valid @RequestBody EventDTO eventDTO) {
        return ResponseEntity.ok(eventService.updateEvent(id, eventDTO));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> cancelEvent(@PathVariable Long id) {
        eventService.cancelEvent(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<Page<EventListItemDTO>> getEvents(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Event.EventType type,
            @AuthenticationPrincipal UserDetails userDetails,
            Pageable pageable) {
        Long userId = userDetails != null ? Long.parseLong(userDetails.getUsername()) : null;
        return ResponseEntity.ok(eventService.searchEvents(keyword, type, userId, pageable));
    }

    @GetMapping("/upcoming")
    public ResponseEntity<Page<EventListItemDTO>> getUpcomingEvents(
            @AuthenticationPrincipal UserDetails userDetails,
            Pageable pageable) {
        Long userId = userDetails != null ? Long.parseLong(userDetails.getUsername()) : null;
        return ResponseEntity.ok(eventService.getUpcomingEvents(userId, pageable));
    }

    @GetMapping("/ongoing")
    public ResponseEntity<Page<EventListItemDTO>> getOngoingEvents(
            @AuthenticationPrincipal UserDetails userDetails,
            Pageable pageable) {
        Long userId = userDetails != null ? Long.parseLong(userDetails.getUsername()) : null;
        return ResponseEntity.ok(eventService.getOngoingEvents(userId, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventDTO> getEvent(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = userDetails != null ? Long.parseLong(userDetails.getUsername()) : null;
        return ResponseEntity.ok(eventService.getEventById(id, userId));
    }

    @PostMapping("/{id}/register")
    public ResponseEntity<EventRegistrationDTO> registerForEvent(
            @PathVariable Long id,
            @Valid @RequestBody EventRegistrationDTO registrationDTO,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        return ResponseEntity.ok(eventService.registerForEvent(id, userId, registrationDTO));
    }

    @DeleteMapping("/{id}/register")
    public ResponseEntity<?> cancelRegistration(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        eventService.cancelRegistration(id, userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/registrations/me")
    public ResponseEntity<Page<EventRegistrationDTO>> getMyRegistrations(
            @AuthenticationPrincipal UserDetails userDetails,
            Pageable pageable) {
        Long userId = Long.parseLong(userDetails.getUsername());
        return ResponseEntity.ok(eventService.getUserRegistrations(userId, pageable));
    }

    @GetMapping("/{id}/registrations")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<EventRegistrationDTO>> getEventRegistrations(
            @PathVariable Long id,
            Pageable pageable) {
        return ResponseEntity.ok(eventService.getEventRegistrations(id, pageable));
    }
}