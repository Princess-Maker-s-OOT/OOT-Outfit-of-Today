package org.example.ootoutfitoftoday.domain.payment.service.command;

import org.example.ootoutfitoftoday.domain.payment.entity.Payment;

public interface PaymentCommandService {

    void failPayment(Long paymentId, String reason);

    void savePayment(Payment payment);
}
