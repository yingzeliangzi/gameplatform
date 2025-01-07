package com.gameplatform.service.impl;

import com.gameplatform.exception.BusinessException;
import com.gameplatform.model.dto.EventDTO;
import com.gameplatform.model.dto.EventListItemDTO;
import com.gameplatform.model.dto.EventRegistrationDTO;
import com.gameplatform.model.dto.NotificationDTO;
import com.gameplatform.model.entity.*;
import com.gameplatform.repository.EventRepository;
import com.gameplatform.repository.EventRegistrationRepository;
import com.gameplatform.repository.UserRepository;
import com.gameplatform.repository.GameRepository;
import com.gameplatform.service.EventService;
import com.gameplatform.service.NotificationService;
import com.gameplatform.util.FileUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
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
    @Transactional
    public void sendEventReminders(Event event) {
        // 获取所有报名用户
        List<EventRegistration> registrations = registrationRepository
                .findByEventIdAndStatus(event.getId(), EventRegistration.RegistrationStatus.REGISTERED);

        // 给每个用户发送提醒
        for (EventRegistration registration : registrations) {
            NotificationDTO notification = new NotificationDTO();
            notification.setTitle("活动即将开始");
            notification.setContent("您报名的活动「" + event.getTitle() + "」将在一小时后开始");
            notification.setType(Notification.NotificationType.EVENT_REMINDER);
            notificationService.sendNotification(registration.getUser().getId(), notification);
        }
    }

    private void notifyNewEvent(Event event) {
        if (event.getGame() != null) {
            List<User> interestedUsers = userRepository.findByOwnedGamesContaining(event.getGame());
            for (User user : interestedUsers) {
                NotificationDTO notification = new NotificationDTO();
                notification.setTitle("新活动发布");
                notification.setContent("您关注的游戏「" + event.getGame().getTitle() + "」发布了新活动：" + event.getTitle());
                notification.setType(Notification.NotificationType.EVENT_REMINDER);
                notificationService.sendNotification(user.getId(), notification);
            }
        }
    }

    private void notifyEventUpdate(Event event) {
        List<EventRegistration> registrations = registrationRepository
                .findByEventIdAndStatus(event.getId(), EventRegistration.RegistrationStatus.REGISTERED);

        for (EventRegistration registration : registrations) {
            NotificationDTO notification = new NotificationDTO();
            notification.setTitle("活动信息更新");
            notification.setContent("您报名的活动「" + event.getTitle() + "」信息有更新");
            notification.setType(Notification.NotificationType.EVENT_REMINDER);
            notificationService.sendNotification(registration.getUser().getId(), notification);
        }
    }

    private void notifyEventCancellation(Event event) {
        List<EventRegistration> registrations = registrationRepository
                .findByEventIdAndStatus(event.getId(), EventRegistration.RegistrationStatus.REGISTERED);

        for (EventRegistration registration : registrations) {
            registration.setStatus(EventRegistration.RegistrationStatus.CANCELLED);
            registrationRepository.save(registration);

            NotificationDTO notification = new NotificationDTO();
            notification.setTitle("活动已取消");
            notification.setContent("您报名的活动「" + event.getTitle() + "」已被取消");
            notification.setType(Notification.NotificationType.EVENT_REMINDER);
            notificationService.sendNotification(registration.getUser().getId(), notification);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EventListItemDTO> searchEvents(String keyword, Event.EventType type, Long userId, Pageable pageable) {
        Page<Event> events;
        if (type != null) {
            events = eventRepository.findByType(type, pageable);
        } else if (keyword != null && !keyword.isEmpty()) {
            events = eventRepository.findByTitleContainingIgnoreCase(keyword, pageable);
        } else {
            events = eventRepository.findAll(pageable);
        }

        List<EventListItemDTO> dtos = events.getContent().stream()
                .map(event -> convertToListItemDTO(event, userId))
                .collect(Collectors.toList());

        return new PageImpl<>(dtos, pageable, events.getTotalElements());
    }

    private EventListItemDTO convertToListItemDTO(Event event, Long userId) {
        EventListItemDTO dto = new EventListItemDTO();
        BeanUtils.copyProperties(event, dto);

        if (userId != null) {
            dto.setRegistered(registrationRepository.existsByEventIdAndUserIdAndStatus(
                    event.getId(),
                    userId,
                    EventRegistration.RegistrationStatus.REGISTERED
            ));
        }

        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Event> getEventsStartingBetween(LocalDateTime start, LocalDateTime end) {
        return eventRepository.findByStartTimeBetween(start, end);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Event> findPendingEvents() {
        return eventRepository.findByStatus(Event.EventStatus.UPCOMING);
    }

    @Override
    @Transactional
    public Event updateEventDetails(Long eventId, Event eventDetails) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new BusinessException("活动不存在"));
        BeanUtils.copyProperties(eventDetails, event, "id", "coverImage", "images", "registrations");
        return eventRepository.save(event);
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
        BeanUtils.copyProperties(eventDTO, event, "id", "coverImage", "images");

        // 设置关联游戏
        if (eventDTO.getGameId() != null) {
            Game game = gameRepository.findById(eventDTO.getGameId())
                    .orElseThrow(() -> new BusinessException("游戏不存在"));
            event.setGame(game);
        }

        // 保存活动
        Event savedEvent = eventRepository.save(event);

        // 通知相关用户
        notifyNewEvent(savedEvent);

        return convertToDTO(savedEvent);
    }

    @Override
    @Transactional
    public EventDTO updateEvent(Long eventId, EventDTO eventDTO) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new BusinessException("活动不存在"));

        // 如果活动已经开始，则不允许修改某些信息
        if (event.getStartTime().isBefore(LocalDateTime.now())) {
            throw new BusinessException("活动已开始，无法修改基本信息");
        }

        BeanUtils.copyProperties(eventDTO, event, "id", "coverImage", "images", "registrations");

        Event updatedEvent = eventRepository.save(event);

        // 通知已报名用户活动信息变更
        notifyEventUpdate(updatedEvent);

        return convertToDTO(updatedEvent);
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

        // 通知已报名用户活动取消
        notifyEventCancellation(event);
    }

    @Override
    @Transactional
    public EventRegistrationDTO registerForEvent(Long eventId, Long userId, EventRegistrationDTO registrationDTO) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new BusinessException("活动不存在"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));

        // 验证活动状态
        if (event.getStatus() != Event.EventStatus.UPCOMING) {
            throw new BusinessException("活动不在报名阶段");
        }

        // 验证是否已报名
        if (registrationRepository.existsByEventIdAndUserIdAndStatus(
                eventId, userId, EventRegistration.RegistrationStatus.REGISTERED)) {
            throw new BusinessException("已经报名过了");
        }

        // 验证人数限制
        if (event.getMaxParticipants() != null &&
                event.getCurrentParticipants() >= event.getMaxParticipants()) {
            throw new BusinessException("活动名额已满");
        }

        // 创建报名记录
        EventRegistration registration = new EventRegistration();
        registration.setEvent(event);
        registration.setUser(user);
        registration.setContactInfo(registrationDTO.getContactInfo());
        registration.setRemark(registrationDTO.getRemark());

        EventRegistration savedRegistration = registrationRepository.save(registration);

        // 更新活动当前人数
        event.setCurrentParticipants(event.getCurrentParticipants() + 1);
        eventRepository.save(event);

        // 发送报名成功通知
        notificationService.sendRegistrationConfirmation(savedRegistration);

        return convertToRegistrationDTO(savedRegistration);
    }

    @Override
    @Transactional
    public void cancelRegistration(Long eventId, Long userId) {
        EventRegistration registration = registrationRepository
                .findByEventIdAndUserId(eventId, userId)
                .orElseThrow(() -> new BusinessException("未找到报名记录"));

        if (registration.getEvent().getStartTime().isBefore(LocalDateTime.now())) {
            throw new BusinessException("活动已开始，无法取消报名");
        }

        registration.setStatus(EventRegistration.RegistrationStatus.CANCELLED);
        registrationRepository.save(registration);

        // 更新活动当前人数
        Event event = registration.getEvent();
        event.setCurrentParticipants(event.getCurrentParticipants() - 1);
        eventRepository.save(event);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EventListItemDTO> getUpcomingEvents(Long userId, Pageable pageable) {
        Page<Event> events = eventRepository.findUpcomingEvents(
                LocalDateTime.now(),
                Event.EventStatus.UPCOMING,
                pageable
        );
        return events.map(event -> convertToListItemDTO(event, userId));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EventListItemDTO> getOngoingEvents(Long userId, Pageable pageable) {
        Page<Event> events = eventRepository.findOngoingEvents(
                LocalDateTime.now(),
                Event.EventStatus.ONGOING,
                pageable
        );
        return events.map(event -> convertToListItemDTO(event, userId));
    }

    // 定时任务：检查活动状态并更新
    @Scheduled(cron = "0 */5 * * * *")  // 每5分钟执行一次
    @Transactional
    public void updateEventStatus() {
        LocalDateTime now = LocalDateTime.now();

        // 更新即将开始的活动
        List<Event> upcomingEvents = eventRepository.findByStartTimeBetween(
                now.minusMinutes(5), now
        );
        upcomingEvents.forEach(event -> {
            event.setStatus(Event.EventStatus.ONGOING);
            eventRepository.save(event);
        });

        // 更新已结束的活动
        List<Event> ongoingEvents = eventRepository.findByEndTimeBetween(
                now.minusMinutes(5), now
        );
        ongoingEvents.forEach(event -> {
            event.setStatus(Event.EventStatus.ENDED);
            eventRepository.save(event);
        });
    }

    // 提醒即将开始的活动
    @Scheduled(cron = "0 0 * * * *")  // 每小时执行一次
    @Transactional
    public void sendEventReminders() {
        // 查找24小时内即将开始的活动
        LocalDateTime now = LocalDateTime.now();
        List<Event> upcomingEvents = eventRepository.findByStartTimeBetween(
                now, now.plusHours(24)
        );

        upcomingEvents.forEach(event -> {
            event.getRegistrations().stream()
                    .filter(reg -> reg.getStatus() == EventRegistration.RegistrationStatus.REGISTERED)
                    .forEach(reg -> notificationService.sendEventReminder(reg));
        });
    }

    public EventServiceImpl(EventRepository eventRepository,
                            EventRegistrationRepository registrationRepository,
                            UserRepository userRepository,
                            GameRepository gameRepository,
                            NotificationService notificationService,
                            FileUtil fileUtil) {
        this.eventRepository = eventRepository;
        this.registrationRepository = registrationRepository;
        this.userRepository = userRepository;
        this.gameRepository = gameRepository;
        this.notificationService = notificationService;
        this.fileUtil = fileUtil;
    }

    private EventDTO convertToDTO(Event event) {
        EventDTO dto = new EventDTO();
        BeanUtils.copyProperties(event, dto);

        if (event.getGame() != null) {
            dto.setGameId(event.getGame().getId());
            dto.setGameName(event.getGame().getTitle());
        }

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

    private EventRegistrationDTO convertToRegistrationDTO(EventRegistration registration) {
        EventRegistrationDTO dto = new EventRegistrationDTO();
        BeanUtils.copyProperties(registration, dto);
        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Event> findByEndTimeBetween(LocalDateTime start, LocalDateTime end) {
        return eventRepository.findByEndTimeBetween(start, end);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Event> findByStatus(Event.EventStatus status) {
        return eventRepository.findByStatus(status);
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
    @Transactional(readOnly = true)
    public boolean checkEventCapacity(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new BusinessException("活动不存在"));
        return event.getMaxParticipants() == null ||
                event.getCurrentParticipants() < event.getMaxParticipants();
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventRegistration> getEventParticipants(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new BusinessException("活动不存在"));
        return event.getRegistrations().stream()
                .filter(r -> r.getStatus() == EventRegistration.RegistrationStatus.REGISTERED)
                .collect(Collectors.toList());
    }
}

