package org.example.ootoutfitoftoday.domain.closet.repository;

import org.example.ootoutfitoftoday.domain.closet.entity.Closet;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClosetRepository extends JpaRepository<Closet, Long> {
}
