package com.gameplatform.service;

import com.gameplatform.model.entity.Event;
import com.gameplatform.model.entity.EventRegistration;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author SakurazawaRyoko
 * @version 1.0
 * @date 2024/12/28 15:12
 * @description TODO
 */
public interface EventService extends BaseEventService {

    // 获取指定时间范围内的事件
    List<Event> getEventsStartingBetween(LocalDateTime start, LocalDateTime end);

    // 查找待处理的事件
    List<Event> findPendingEvents();

    // 更新事件状态
    void updateEventStatus(Long eventId, Event.EventStatus status);

    // 检查事件容量
    boolean checkEventCapacity(Long eventId);

    // 获取事件参与者
    List<EventRegistration> getEventParticipants(Long eventId);

    // 取消事件
    void cancelEvent(Long eventId);

    // 更新事件详情
    Event updateEventDetails(Long eventId, Event eventDetails);

    // 获取用户的事件
    List<Event> getUserEvents(Long userId, Pageable pageable);

    // 发送事件提醒
    void sendEventReminders(Event event);
}