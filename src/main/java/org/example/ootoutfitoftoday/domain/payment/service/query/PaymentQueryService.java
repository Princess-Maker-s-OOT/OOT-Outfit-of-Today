package org.example.ootoutfitoftoday.domain.payment.service.query;

public interface PaymentQueryService {

    boolean existsByTossOrderId(String tossOrderId);
}
