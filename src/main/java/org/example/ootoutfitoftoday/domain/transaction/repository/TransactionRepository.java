package org.example.ootoutfitoftoday.domain.transaction.repository;

import org.example.ootoutfitoftoday.domain.transaction.entity.Transaction;
import org.example.ootoutfitoftoday.domain.transaction.enums.TransactionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    Optional<Transaction> findBySalePostIdAndStatusIn(
            Long salePostId,
            List<TransactionStatus> statuses
    );
}
