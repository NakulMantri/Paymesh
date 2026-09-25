package com.paymesh.paymentservice.event;

import com.paymesh.common.events.PaymentEvent;

public interface PaymentEventPublisher {
    void publishPaymentEvent(PaymentEvent event);
}
