package org.example.ootoutfitoftoday.domain.auth.enums;

/**
 * RefreshToken의 상태를 나타내는 enum
 * - ACTIVE: 정상 사용 가능한 토큰
 * - REVOKED: 로그아웃 등으로 무효화된 토큰
 */
public enum RefreshTokenStatus {

    ACTIVE,   // 활성 (정상)
    REVOKED   // 무효화 (로그아웃, 회원탈퇴)
}
