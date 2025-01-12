package com.gameplatform.service.impl;

import com.gameplatform.exception.BusinessException;
import com.gameplatform.model.dto.EventDTO;
import com.gameplatform.model.dto.EventListItemDTO;
import com.gameplatform.model.dto.EventRegistrationDTO;
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
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
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
    private final EventRegistrationRepository eventRegistrationRepository;
    private final EventRegistrationRepository registrationRepository;
    private final UserRepository userRepository;
    private final GameRepository gameRepository;
    private final NotificationService notificationService;
    private final FileUtil fileUtil;

    private EventRegistrationDTO convertToEventRegistrationDTO(EventRegistration registration) {
        EventRegistrationDTO dto = new EventRegistrationDTO();
        dto.setId(registration.getId());
        dto.setEventId(registration.getEvent().getId());
        dto.setUserId(registration.getUser().getId());
        dto.setUsername(registration.getUser().getUsername());
        dto.setContactInfo(registration.getContactInfo());
        dto.setRemark(registration.getRemark());
        dto.setStatus(registration.getStatus());
        dto.setRegisteredAt(registration.getRegisteredAt());
        dto.setCancelledAt(registration.getCancelledAt());
        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EventDTO> getUserEvents(Long userId, Pageable pageable) {
        // 获取分页的活动报名记录
        Page<EventRegistration> registrations = eventRegistrationRepository
                .findByUserIdOrderByRegisteredAtDesc(userId, pageable);

        // 将 Page<EventRegistration> 转换为 Page<EventDTO>
        return registrations.map(registration -> {
            Event event = registration.getEvent();
            return convertToDTO(event);
        });
    }

    private EventDTO convertToDTO(Event event) {
        EventDTO dto = new EventDTO();
        BeanUtils.copyProperties(event, dto);

        if (event.getGame() != null) {
            dto.setGameId(event.getGame().getId());
            dto.setGameName(event.getGame().getTitle());
        }

        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EventRegistrationDTO> getUserRegistrations(Long userId, Pageable pageable) {
        return eventRegistrationRepository.findByUserId(userId, pageable)
                .map(this::convertToEventRegistrationDTO);
    }

    @Override
    @Transactional
    public void sendRegistrationConfirmation(EventRegistration registration) {
        Event event = registration.getEvent();
        User user = registration.getUser();

        // 创建通知
        NotificationDTO notification = new NotificationDTO();
        notification.setTitle("报名确认通知");
        notification.setContent(String.format("您已成功报名活动「%s」，活动时间：%s",
                event.getTitle(),
                formatEventTime(event.getStartTime(), event.getEndTime())));
        notification.setType(Notification.NotificationType.EVENT_REMINDER);
        notification.setTargetType("EVENT");
        notification.setTargetId(event.getId());

        // 发送通知
        notificationService.sendNotification(user.getId(), notification);
    }

    private String formatEventTime(LocalDateTime startTime, LocalDateTime endTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        return String.format("%s 至 %s",
                startTime.format(formatter),
                endTime.format(formatter));
    }

    @Override
    @Transactional
    public void sendEventCancellationNotice(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new BusinessException("活动不存在"));

        // 获取所有已报名的用户
        List<EventRegistration> registrations = eventRegistrationRepository
                .findByEventIdAndStatus(eventId, EventRegistration.RegistrationStatus.REGISTERED);

        // 发送取消通知给所有报名用户
        for (EventRegistration registration : registrations) {
            NotificationDTO notification = new NotificationDTO();
            notification.setTitle("活动取消通知");
            notification.setContent("您报名的活动「" + event.getTitle() + "」已被取消");
            notification.setType(Notification.NotificationType.EVENT_REMINDER);
            notification.setTargetType("EVENT");
            notification.setTargetId(event.getId());

            notificationService.sendNotification(registration.getUser().getId(), notification);
        }

        // 更新活动状态
        event.setStatus(Event.EventStatus.CANCELLED);
        eventRepository.save(event);

        // 更新所有报名状态
        registrations.forEach(registration -> {
            registration.setStatus(EventRegistration.RegistrationStatus.CANCELLED);
            registration.setCancelledAt(LocalDateTime.now());
        });
        eventRegistrationRepository.saveAll(registrations);
    }

    @Override
    @Transactional(readOnly = true)
    public long countOngoingEvents() {
        LocalDateTime now = LocalDateTime.now();
        return eventRepository.countByStatusAndStartTimeBeforeAndEndTimeAfter(
                Event.EventStatus.ONGOING,
                now,
                now
        );
    }

    @Override
    @Transactional(readOnly = true)
    public long countUpcomingEvents() {
        return eventRepository.countByStatusAndStartTimeAfter(
                Event.EventStatus.UPCOMING,
                LocalDateTime.now()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public long countEventsByGameId(Long gameId) {
        return eventRepository.countByGameId(gameId);
    }

    @Override
    @Transactional(readOnly = true)
    public long countEventsByUserId(Long userId) {
        return eventRegistrationRepository.countByUserId(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public double getParticipationRate(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new BusinessException("活动不存在"));

        if (event.getMaxParticipants() == null || event.getMaxParticipants() == 0) {
            return 0.0;  // 如果没有设置最大参与人数，返回0
        }

        int currentParticipants = event.getCurrentParticipants();
        int maxParticipants = event.getMaxParticipants();

        // 计算参与率（百分比）
        return (double) currentParticipants / maxParticipants * 100;
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventDTO> getRecommendedEvents(Long userId, int limit) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));

        // 获取用户的游戏列表
        Set<Game> userGames = user.getGames().stream()
                .map(UserGame::getGame)
                .collect(Collectors.toSet());

        // 获取相关游戏的活动
        LocalDateTime now = LocalDateTime.now();
        List<Event> events = eventRepository.findByStatusAndGameIn(
                Event.EventStatus.UPCOMING,
                userGames,
                PageRequest.of(0, limit, Sort.by(Sort.Direction.ASC, "startTime"))
        );

        return events.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventDTO> getPopularEvents(int limit) {
        // 获取报名人数最多的活动
        Page<Event> events = eventRepository.findAll(PageRequest.of(0, limit,
                Sort.by(Sort.Direction.DESC, "currentParticipants")));

        return events.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventDTO> getEventsNearby(Double latitude, Double longitude, Double radiusInKm, int limit) {
        // 由于没有地理位置字段，这里先实现一个简化版本
        // 在实际应用中，应该使用空间数据库功能来实现
        LocalDateTime now = LocalDateTime.now();

        // 获取所有即将开始的活动
        List<Event> events = eventRepository.findByStartTimeBetween(
                now,
                now.plusDays(7)  // 获取一周内的活动
        );

        // 按照开始时间排序并限制数量
        return events.stream()
                .filter(event -> event.getStatus() == Event.EventStatus.UPCOMING)
                .sorted(Comparator.comparing(Event::getStartTime))
                .limit(limit)
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void batchUpdateEventStatus(List<Long> eventIds, Event.EventStatus status) {
        List<Event> events = eventRepository.findAllById(eventIds);
        events.forEach(event -> event.setStatus(status));
        eventRepository.saveAll(events);

        // 如果状态更新为取消，则通知相关用户
        if (status == Event.EventStatus.CANCELLED) {
            events.forEach(this::notifyEventCancellation);
        }
    }

    // 修复通知相关的错误，新增发送事件通知的方法
    private void notifyNewEvent(Event event) {
        if (event.getGame() != null) {
            List<User> interestedUsers = userRepository.findByOwnedGamesContaining(event.getGame());
            interestedUsers.forEach(user -> {
                NotificationDTO notification = new NotificationDTO();
                notification.setTitle("新活动通知");
                notification.setContent("您关注的游戏有新活动：" + event.getTitle());
                notification.setType(Notification.NotificationType.EVENT_REMINDER);
                notification.setTargetType("EVENT");
                notification.setTargetId(event.getId());
                notificationService.sendNotification(user.getId(), notification);
            });
        }
    }

    // 修改导致类型不匹配的部分
    @Override
    public void sendEventReminders(Event event) {
        List<EventRegistration> registrations = registrationRepository
                .findByEventIdAndStatus(event.getId(), EventRegistration.RegistrationStatus.REGISTERED);

        registrations.forEach(registration -> {
            NotificationDTO notification = new NotificationDTO();
            notification.setTitle("活动提醒");
            notification.setContent("您报名的活动「" + event.getTitle() + "」即将开始");
            notification.setType(Notification.NotificationType.EVENT_REMINDER);
            notification.setTargetType("EVENT");
            notification.setTargetId(event.getId());
            notificationService.sendNotification(registration.getUser().getId(), notification);
        });
    }

    @Override
    @Transactional
    public void deleteEvent(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new BusinessException("活动不存在"));

        // 检查活动状态
        if (event.getStatus() == Event.EventStatus.ONGOING) {
            throw new BusinessException("无法删除正在进行的活动");
        }

        // 取消相关的报名
        List<EventRegistration> registrations = registrationRepository
                .findByEventIdAndStatus(eventId, EventRegistration.RegistrationStatus.REGISTERED);

        registrations.forEach(registration -> {
            registration.setStatus(EventRegistration.RegistrationStatus.CANCELLED);
            registration.setCancelledAt(LocalDateTime.now());
            notificationService.sendEventReminder(registration);
        });

        eventRepository.delete(event);
    }

    @Override
    @Transactional
    public void batchDeleteEvents(List<Long> eventIds) {
        List<Event> events = eventRepository.findAllById(eventIds);

        // 检查每个事件的状态
        events.forEach(event -> {
            if (event.getStatus() == Event.EventStatus.ONGOING) {
                throw new BusinessException("无法删除正在进行的活动");
            }
        });

        // 取消相关的报名
        events.forEach(event -> {
            List<EventRegistration> registrations = registrationRepository
                    .findByEventIdAndStatus(event.getId(), EventRegistration.RegistrationStatus.REGISTERED);

            registrations.forEach(registration -> {
                registration.setStatus(EventRegistration.RegistrationStatus.CANCELLED);
                registration.setCancelledAt(LocalDateTime.now());
                // 发送通知
                notificationService.sendEventReminder(registration);
            });
        });

        eventRepository.deleteAll(events);
    }

    // 修复通知发送相关的方法
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

        registrations.forEach(registration ->
                notificationService.sendEventReminder(registration));
    }

    private EventListItemDTO convertToEventListItemDTO(Event event) {
        EventListItemDTO dto = new EventListItemDTO();
        BeanUtils.copyProperties(event, dto);

        if (event.getGame() != null) {
            dto.setGame(event.getGame());
        }

        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EventDTO> getOngoingEvents(Long userId, Pageable pageable) {
        LocalDateTime now = LocalDateTime.now();
        Page<Event> events = eventRepository.findByStartTimeBeforeAndEndTimeAfterAndStatus(
                now, now, Event.EventStatus.ONGOING, pageable);
        return events.map(this::convertToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EventDTO> getUpcomingEvents(Long userId, Pageable pageable) {
        LocalDateTime now = LocalDateTime.now();
        Page<Event> events = eventRepository.findByStartTimeAfterAndStatus(
                now, Event.EventStatus.UPCOMING, pageable);
        return events.map(this::convertToDTO);
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

    @Override
    @Transactional
    public EventRegistrationDTO registerForEvent(Long eventId, Long userId, EventRegistrationDTO registrationDTO) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new BusinessException("活动不存在"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));

        if (eventRegistrationRepository.existsByEventIdAndUserIdAndStatus(
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

        EventRegistration savedRegistration = eventRegistrationRepository.save(registration);

        // 更新活动参与人数
        event.setCurrentParticipants(event.getCurrentParticipants() + 1);
        eventRepository.save(event);

        return convertToDTO(savedRegistration);
    }

    private EventRegistrationDTO convertToDTO(EventRegistration registration) {
        EventRegistrationDTO dto = new EventRegistrationDTO();
        BeanUtils.copyProperties(registration, dto);
        return dto;
    }

    @Override
    @Transactional
    public void batchCancelRegistrations(Long eventId, List<Long> userIds) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new BusinessException("活动不存在"));

        for (Long userId : userIds) {
            EventRegistration registration = eventRegistrationRepository
                    .findByEventIdAndUserId(eventId, userId)
                    .orElseThrow(() -> new BusinessException("未找到报名记录"));

            registration.setStatus(EventRegistration.RegistrationStatus.CANCELLED);
            registration.setCancelledAt(LocalDateTime.now());
            eventRegistrationRepository.save(registration);
        }

        // 更新活动参与人数
        event.setCurrentParticipants(Math.max(0,
                event.getCurrentParticipants() - userIds.size()));
        eventRepository.save(event);
    }

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
    @Transactional(readOnly = true)
    public List<EventRegistration> getEventParticipants(Long eventId) {
        return eventRegistrationRepository.findByEventIdAndStatus(
                eventId,
                EventRegistration.RegistrationStatus.REGISTERED
        );
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EventRegistrationDTO> getEventRegistrations(Long eventId, Pageable pageable) {
        // 使用分页查询
        Page<EventRegistration> registrationPage = eventRegistrationRepository
                .findByEventIdOrderByRegisteredAtDesc(eventId, pageable);

        // 转换为DTO
        return registrationPage.map(registration -> {
            EventRegistrationDTO dto = new EventRegistrationDTO();
            dto.setId(registration.getId());
            dto.setEventId(registration.getEvent().getId());
            dto.setUserId(registration.getUser().getId());
            dto.setUsername(registration.getUser().getUsername());
            dto.setContactInfo(registration.getContactInfo());
            dto.setRemark(registration.getRemark());
            dto.setStatus(registration.getStatus());
            dto.setRegisteredAt(registration.getRegisteredAt());
            dto.setCancelledAt(registration.getCancelledAt());
            return dto;
        });
    }
}