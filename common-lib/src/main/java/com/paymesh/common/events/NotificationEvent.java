package com.paymesh.common.events;

import java.time.LocalDateTime;
import java.util.Map;

public class NotificationEvent {
    private String eventId;
    private Long userId;
    private String recipientEmail;
    private String recipientPhone;
    private String channel;
    private String template;
    private String subject;
    private String message;
    private Map<String, Object> metadata;
    private String traceId;
    private LocalDateTime timestamp;

    public NotificationEvent() {
        this.timestamp = LocalDateTime.now();
    }

    public NotificationEvent(String eventId, Long userId, String recipientEmail, String recipientPhone, String channel, String template, String subject, String message, Map<String, Object> metadata, String traceId, LocalDateTime timestamp) {
        this.eventId = eventId;
        this.userId = userId;
        this.recipientEmail = recipientEmail;
        this.recipientPhone = recipientPhone;
        this.channel = channel;
        this.template = template;
        this.subject = subject;
        this.message = message;
        this.metadata = metadata;
        this.traceId = traceId;
        this.timestamp = timestamp != null ? timestamp : LocalDateTime.now();
    }

    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getRecipientEmail() { return recipientEmail; }
    public void setRecipientEmail(String recipientEmail) { this.recipientEmail = recipientEmail; }

    public String getRecipientPhone() { return recipientPhone; }
    public void setRecipientPhone(String recipientPhone) { this.recipientPhone = recipientPhone; }

    public String getChannel() { return channel; }
    public void setChannel(String channel) { this.channel = channel; }

    public String getTemplate() { return template; }
    public void setTemplate(String template) { this.template = template; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public Map<String, Object> getMetadata() { return metadata; }
    public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }

    public String getTraceId() { return traceId; }
    public void setTraceId(String traceId) { this.traceId = traceId; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}
