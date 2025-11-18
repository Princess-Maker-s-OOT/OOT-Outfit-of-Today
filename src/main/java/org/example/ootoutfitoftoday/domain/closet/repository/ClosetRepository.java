package org.example.ootoutfitoftoday.domain.closet.repository;

import org.example.ootoutfitoftoday.domain.closet.entity.Closet;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClosetRepository extends JpaRepository<Closet, Long> {

    Page<Closet> findAllByIsPublicTrue(Pageable pageable);

    Page<Closet> findAllByUser_Id(Long userId, Pageable pageable);
}