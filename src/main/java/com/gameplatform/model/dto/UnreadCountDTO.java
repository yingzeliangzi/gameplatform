package com.gameplatform.model.dto;

import lombok.Data;

@Data
public class UnreadCountDTO {
    private long total;
    private long system;
    private long gameDiscount;
    private long eventReminder;
    private long postReply;
}
