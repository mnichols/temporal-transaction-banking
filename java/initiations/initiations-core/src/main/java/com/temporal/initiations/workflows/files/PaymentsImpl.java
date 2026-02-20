package com.temporal.initiations.workflows.files;

import com.temporal.initiations.messages.domain.workflows.Payment;

public class PaymentsImpl implements Payments {
    @Override
    public void persistPayment(Payment payment) {
        // write payment to db
    }
}
