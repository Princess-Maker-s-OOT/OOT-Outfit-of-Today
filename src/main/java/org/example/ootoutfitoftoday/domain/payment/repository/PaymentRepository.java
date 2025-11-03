package org.example.ootoutfitoftoday.domain.payment.repository;

import org.example.ootoutfitoftoday.domain.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByTossOrderId(String tossOrderId);
}
