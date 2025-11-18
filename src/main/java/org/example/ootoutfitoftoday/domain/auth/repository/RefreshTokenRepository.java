package org.example.ootoutfitoftoday.domain.auth.repository;

import org.example.ootoutfitoftoday.domain.auth.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    Optional<RefreshToken> findByUserIdAndDeviceId(Long userId, String deviceId);

    List<RefreshToken> findAllByUserIdOrderByLastUsedAtDesc(Long userId);

    Optional<RefreshToken> findTopByUserIdOrderByLastUsedAtAsc(Long userId);

    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM RefreshToken rt WHERE rt.user.id = :userId")
    void deleteByUserId(@Param("userId") Long userId);

    void deleteByUserIdAndDeviceId(Long userId, String deviceId);

    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM RefreshToken rt WHERE rt.expiresAt < :now")
    void deleteExpiredTokens(@Param("now") LocalDateTime now);

    long countByUserId(Long userId);
}
