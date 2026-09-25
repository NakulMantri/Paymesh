package com.paymesh.notificationservice.consumer;

import com.paymesh.common.dto.payment.PaymentStatus;
import com.paymesh.common.events.PaymentEvent;
import com.paymesh.notificationservice.entity.NotificationLog;
import com.paymesh.notificationservice.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentEventConsumerTest {

    @Mock
    private NotificationRepository notificationRepository;

    private PaymentEventConsumer consumer;

    @BeforeEach
    void setUp() {
        consumer = new PaymentEventConsumer(notificationRepository);
    }

    @Test
    void testHandlePaymentCompletedEvent() {
        PaymentEvent event = new PaymentEvent(
                "EVT-1", "PAYMENT_COMPLETED", "PAY-99", 1L, "ELEC-CONED", "1234567890",
                new BigDecimal("150.00"), PaymentStatus.COMPLETED, null, "trace-abc", LocalDateTime.now()
        );

        consumer.handlePaymentEvent(event);

        ArgumentCaptor<NotificationLog> captor = ArgumentCaptor.forClass(NotificationLog.class);
        verify(notificationRepository, times(2)).save(captor.capture());

        List<NotificationLog> savedLogs = captor.getAllValues();
        assertEquals(2, savedLogs.size());

        NotificationLog emailLog = savedLogs.stream().filter(l -> "EMAIL".equals(l.getChannel())).findFirst().orElseThrow();
        assertTrue(emailLog.getSubject().contains("Successful"));
        assertEquals("DELIVERED", emailLog.getStatus());

        NotificationLog smsLog = savedLogs.stream().filter(l -> "SMS".equals(l.getChannel())).findFirst().orElseThrow();
        assertEquals("DELIVERED", smsLog.getStatus());
    }
}
