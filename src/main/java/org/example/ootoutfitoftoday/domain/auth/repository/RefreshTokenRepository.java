package org.example.ootoutfitoftoday.domain.auth.repository;

import org.example.ootoutfitoftoday.domain.auth.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

// 연관관계 없음. userId(Long)로 조회 및 삭제
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    // 토큰 값으로 조회
    Optional<RefreshToken> findByToken(String token);

    // userId로 조회(연관관계 없으므로 Long 타입 사용)
    Optional<RefreshToken> findByUserId(Long userId);

    // userId로 삭제(회원탈퇴, 로그아웃 시 사용)
    void deleteByUserId(Long userId);
}
