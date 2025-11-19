package org.example.ootoutfitoftoday.domain.payment.service.query;

import lombok.RequiredArgsConstructor;
import org.example.ootoutfitoftoday.domain.payment.repository.PaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PaymentQueryServiceImpl implements PaymentQueryService {

    private final PaymentRepository paymentRepository;

    @Override
    public boolean existsByTossOrderId(String tossOrderId) {
        return paymentRepository.existsByTossOrderId(tossOrderId);
    }
}
