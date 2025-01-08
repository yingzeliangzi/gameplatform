package com.gameplatform.service.impl;

import com.gameplatform.exception.BusinessException;
import com.gameplatform.model.dto.EventDTO;
import com.gameplatform.model.dto.EventListItemDTO;
import com.gameplatform.model.dto.NotificationDTO;
import com.gameplatform.model.entity.*;
import com.gameplatform.repository.EventRepository;
import com.gameplatform.repository.EventRegistrationRepository;
import com.gameplatform.repository.GameRepository;
import com.gameplatform.repository.UserRepository;
import com.gameplatform.service.EventService;
import com.gameplatform.service.NotificationService;
import com.gameplatform.util.FileUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author SakurazawaRyoko
 * @version 1.0
 * @date 2024/12/28 19:35
 * @description TODO
 */
@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {
    private final EventRepository eventRepository;
    private final EventRegistrationRepository registrationRepository;
    private final UserRepository userRepository;
    private final GameRepository gameRepository;
    private final NotificationService notificationService;
    private final FileUtil fileUtil;

    @Override
    @Transactional(readOnly = true)
    public EventDTO getEventById(Long id, Long userId) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new BusinessException("活动不存在"));
        EventDTO dto = convertToDTO(event);

        if (userId != null) {
            dto.setRegistered(registrationRepository.existsByEventIdAndUserIdAndStatus(
                    id, userId, EventRegistration.RegistrationStatus.REGISTERED));
        }

        return dto;
    }

    @Override
    @Transactional
    public void updateEventStatus(Long eventId, Event.EventStatus status) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new BusinessException("活动不存在"));
        event.setStatus(status);
        eventRepository.save(event);
    }

    @Override
    @Transactional
    public void cancelEvent(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new BusinessException("活动不存在"));

        if (event.getStatus() == Event.EventStatus.ENDED) {
            throw new BusinessException("活动已结束，无法取消");
        }

        event.setStatus(Event.EventStatus.CANCELLED);
        eventRepository.save(event);

        // 通知已报名用户
        notifyEventCancellation(event);
    }

    @Override
    @Transactional
    public EventDTO createEvent(EventDTO eventDTO) {
        // 验证时间
        if (eventDTO.getStartTime().isBefore(LocalDateTime.now())) {
            throw new BusinessException("活动开始时间不能早于当前时间");
        }
        if (eventDTO.getEndTime().isBefore(eventDTO.getStartTime())) {
            throw new BusinessException("活动结束时间不能早于开始时间");
        }

        Event event = new Event();
        updateEventFromDTO(event, eventDTO);

        Event savedEvent = eventRepository.save(event);
        // 通知相关用户
        notifyNewEvent(savedEvent);

        return convertToDTO(savedEvent);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EventDTO> searchEvents(String keyword, Event.EventType type, Long userId, Pageable pageable) {
        return eventRepository.findBySearchCriteria(keyword, type, pageable)
                .map(event -> {
                    EventDTO dto = convertToDTO(event);
                    if (userId != null) {
                        dto.setRegistered(registrationRepository.existsByEventIdAndUserIdAndStatus(
                                event.getId(), userId, EventRegistration.RegistrationStatus.REGISTERED));
                    }
                    return dto;
                });
    }

    @Override
    @Transactional
    public EventDTO updateEvent(Long eventId, EventDTO eventDTO) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new BusinessException("活动不存在"));

        if (event.getStartTime().isBefore(LocalDateTime.now())) {
            throw new BusinessException("活动已开始，无法修改");
        }

        updateEventFromDTO(event, eventDTO);
        Event updatedEvent = eventRepository.save(event);

        // 通知已报名用户
        notifyEventUpdate(updatedEvent);

        return convertToDTO(updatedEvent);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EventDTO> getOngoingEvents(Long userId, Pageable pageable) {
        Page<Event> events = eventRepository.findOngoingEvents(
                LocalDateTime.now(),
                Event.EventStatus.ONGOING,
                pageable
        );
        return convertToEventDTOPage(events, userId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EventDTO> getUpcomingEvents(Long userId, Pageable pageable) {
        Page<Event> events = eventRepository.findUpcomingEvents(
                LocalDateTime.now(),
                Event.EventStatus.UPCOMING,
                pageable
        );
        return convertToEventDTOPage(events, userId, pageable);
    }

    @Override
    @Transactional
    public EventDTO registerForEvent(Long eventId, Long userId, EventDTO registrationDTO) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new BusinessException("活动不存在"));

        // 验证活动状态和容量
        validateEventRegistration(event);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));

        if (registrationRepository.existsByEventIdAndUserIdAndStatus(
                eventId, userId, EventRegistration.RegistrationStatus.REGISTERED)) {
            throw new BusinessException("已经报名过该活动");
        }

        EventRegistration registration = new EventRegistration();
        registration.setEvent(event);
        registration.setUser(user);
        registration.setContactInfo(registrationDTO.getContactInfo());
        registration.setRemark(registrationDTO.getRemark());
        registration.setStatus(EventRegistration.RegistrationStatus.REGISTERED);
        registration.setRegisteredAt(LocalDateTime.now());

        registrationRepository.save(registration);

        // 更新活动参与人数
        event.setCurrentParticipants(event.getCurrentParticipants() + 1);
        eventRepository.save(event);

        return convertToDTO(event);
    }

    private void validateEventRegistration(Event event) {
        if (event.getStatus() != Event.EventStatus.UPCOMING) {
            throw new BusinessException("活动不在报名阶段");
        }
        if (event.getMaxParticipants() != null &&
                event.getCurrentParticipants() >= event.getMaxParticipants()) {
            throw new BusinessException("活动名额已满");
        }
    }

    @Override
    @Transactional
    public void cancelRegistration(Long eventId, Long userId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new BusinessException("活动不存在"));

        EventRegistration registration = registrationRepository.findByEventIdAndUserId(eventId, userId)
                .orElseThrow(() -> new BusinessException("未找到报名记录"));

        if (event.getStartTime().isBefore(LocalDateTime.now())) {
            throw new BusinessException("活动已开始，无法取消报名");
        }

        registration.setStatus(EventRegistration.RegistrationStatus.CANCELLED);
        registration.setCancelledAt(LocalDateTime.now());
        registrationRepository.save(registration);

        // 更新活动参与人数
        event.setCurrentParticipants(Math.max(0, event.getCurrentParticipants() - 1));
        eventRepository.save(event);
    }

    private void notifyEventCancellation(Event event) {
        List<EventRegistration> registrations = registrationRepository
                .findByEventIdAndStatus(event.getId(), EventRegistration.RegistrationStatus.REGISTERED);

        registrations.forEach(registration -> {
            registration.setStatus(EventRegistration.RegistrationStatus.CANCELLED);
            registrationRepository.save(registration);

            notificationService.sendEventReminder(registration);
        });
    }

    private void notifyEventUpdate(Event event) {
        List<EventRegistration> registrations = registrationRepository
                .findByEventIdAndStatus(event.getId(), EventRegistration.RegistrationStatus.REGISTERED);

        registrations.forEach(notificationService::sendEventReminder);
    }

    private void notifyNewEvent(Event event) {
        if (event.getGame() != null) {
            List<User> interestedUsers = userRepository.findByOwnedGamesContaining(event.getGame());
            interestedUsers.forEach(user ->
                    notificationService.sendNewPostNotification(user, event)
            );
        }
    }

    private Page<EventDTO> convertToEventDTOPage(Page<Event> events, Long userId, Pageable pageable) {
        List<EventDTO> dtos = events.getContent().stream()
                .map(event -> {
                    EventDTO dto = convertToDTO(event);
                    if (userId != null) {
                        dto.setRegistered(registrationRepository.existsByEventIdAndUserIdAndStatus(
                                event.getId(), userId, EventRegistration.RegistrationStatus.REGISTERED));
                    }
                    return dto;
                })
                .collect(Collectors.toList());

        return new PageImpl<>(dtos, pageable, events.getTotalElements());
    }

    private void updateEventFromDTO(Event event, EventDTO dto) {
        event.setTitle(dto.getTitle());
        event.setDescription(dto.getDescription());
        event.setStartTime(dto.getStartTime());
        event.setEndTime(dto.getEndTime());
        event.setMaxParticipants(dto.getMaxParticipants());
        event.setLocation(dto.getLocation());
        event.setIsOnline(dto.getIsOnline());
        event.setType(dto.getType());
        event.setStatus(Event.EventStatus.UPCOMING);

        if (dto.getGameId() != null) {
            Game game = gameRepository.findById(dto.getGameId())
                    .orElseThrow(() -> new BusinessException("游戏不存在"));
            event.setGame(game);
        }

        // 处理图片
        if (dto.getCoverImage() != null && !dto.getCoverImage().startsWith("/api/files")) {
            event.setCoverImage(dto.getCoverImage());
        }
    }

    private EventDTO convertToDTO(Event event) {
        EventDTO dto = new EventDTO();
        BeanUtils.copyProperties(event, dto);

        if (event.getGame() != null) {
            dto.setGameId(event.getGame().getId());
            dto.setGameName(event.getGame().getTitle());
        }

        // 处理图片URL
        if (event.getCoverImage() != null) {
            dto.setCoverImage(fileUtil.getFileUrl(event.getCoverImage()));
        }
        if (event.getImages() != null) {
            dto.setImages(event.getImages().stream()
                    .map(fileUtil::getFileUrl)
                    .collect(Collectors.toSet()));
        }

        return dto;
    }

    // 以下是接口方法的实现
    @Override
    public List<Event> getEventsStartingBetween(LocalDateTime start, LocalDateTime end) {
        return eventRepository.findByStartTimeBetween(start, end);
    }

    @Override
    public List<Event> findPendingEvents() {
        return eventRepository.findByStatus(Event.EventStatus.UPCOMING);
    }

    @Override
    public boolean checkEventCapacity(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new BusinessException("活动不存在"));
        return event.getMaxParticipants() == null ||
                event.getCurrentParticipants() < event.getMaxParticipants();
    }

    @Override
    public Event updateEventDetails(Long eventId, Event eventDetails) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new BusinessException("活动不存在"));
        BeanUtils.copyProperties(eventDetails, event, "id", "registrations");
        return eventRepository.save(event);
    }

    @Override
    public List<Event> findByEndTimeBetween(LocalDateTime start, LocalDateTime end) {
        return eventRepository.findByEndTimeBetween(start, end);
    }

    @Override
    public List<Event> findByStatus(Event.EventStatus status) {
        return eventRepository.findByStatus(status);
    }

    @Override
    public void sendEventReminders(Event event) {
        List<EventRegistration> registrations = registrationRepository
                .findByEventIdAndStatus(event.getId(), EventRegistration.RegistrationStatus.REGISTERED);

        registrations.forEach(registration ->
                notificationService.sendEventReminder(registration));
    }

    @Override
    public List<EventRegistration> getEventParticipants(Long eventId) {
        return registrationRepository.findByEventIdAndStatus(
                eventId, EventRegistration.RegistrationStatus.REGISTERED);
    }
}