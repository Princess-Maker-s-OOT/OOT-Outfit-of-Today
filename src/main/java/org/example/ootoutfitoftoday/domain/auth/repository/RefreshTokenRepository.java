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

    // 토큰 값으로 조회
    Optional<RefreshToken> findByToken(String token);

    // userId로 조회(연관관계 없으므로 Long 타입 사용)
    // 특정 사용자의 특정 디바이스 토큰 조회
    Optional<RefreshToken> findByUserIdAndDeviceId(Long userId, String deviceId);

    // 특정 사용자의 모든 토큰 조회(최근 사용순)
    List<RefreshToken> findAllByUserIdOrderByLastUsedAtDesc(Long userId);

    // 특정 사용자의 가장 오래된 1건 조회
    Optional<RefreshToken> findTopByUserIdOrderByLastUsedAtAsc(Long userId);

    // userId로 삭제(회원탈퇴, 로그아웃 시 사용)
    // 모든 디바이스 삭제
    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM RefreshToken rt WHERE rt.user.id = :userId")
    void deleteByUserId(@Param("userId") Long userId);

    // 특정 사용자의 특정 디바이스 토큰만 삭제
    void deleteByUserIdAndDeviceId(Long userId, String deviceId);

    // 만료된 토큰 일괄 삭제
    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM RefreshToken rt WHERE rt.expiresAt < :now")
    void deleteExpiredTokens(@Param("now") LocalDateTime now);

    // 특정 사용자의 디바이스 수 카운트(제한 체크용)
    long countByUserId(Long userId);

    // TODO: 임시용. 추후 삭제 예정
    Optional<RefreshToken> findByUserId(Long userId);
}
