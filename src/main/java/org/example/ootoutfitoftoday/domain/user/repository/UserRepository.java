package org.example.ootoutfitoftoday.domain.user.repository;

import org.example.ootoutfitoftoday.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long>, CustomUserRepository {

    boolean existsByLoginId(String loginId);

    boolean existsByEmail(String email);

    boolean existsByNickname(String nickname);

    boolean existsByPhoneNumber(String phoneNumber);

    Optional<User> findByLoginIdAndIsDeletedFalse(String loginId);

    Optional<User> findByIdAndIsDeletedFalse(Long id);

    // 전체 유저 수
    @Query("""
            SELECT count(u)
            FROM User u
            WHERE u.role = 'ROLE_USER'
            """)
    int countAllUsers();

    // 활성 or 비활성 유저 수
    @Query("""
            SELECT count(u)
            FROM User u
            WHERE u.role = 'ROLE_USER'
              AND u.isDeleted = :isDeleted
            """)
    int countByIsDeleted(Boolean isDeleted);

    // 신규 가입자 수 (기간별 집계)
    @Query("""
            SELECT count(u)
            FROM User u
            WHERE u.role = 'ROLE_USER'
              AND u.createdAt >= :start
              AND u.createdAt < :end
            """)
    int countUsersRegisteredSince(LocalDateTime start, LocalDateTime end);

    @Modifying
    @Transactional
    @Query(value = """
            INSERT INTO users 
                (login_id, email, nickname, username, password, 
                 phone_number, role, trade_address, trade_location, image_url, is_deleted)
            VALUES (?1, ?2, ?3, ?4, ?5, ?6, ?7, ?8, ST_GeomFromText(?9, 4326), ?10, ?11)
            """, nativeQuery = true
    )
    void saveAsNativeQuery(
            String loginId,
            String email,
            String nickname,
            String username,
            String password,
            String phoneNumber,
            String role,
            String tradeAddress,
            String tradeLocation,
            String imageUrl,
            boolean isDeleted
    );

    @Modifying
    @Transactional
    @Query(value = """
            UPDATE users 
            SET trade_address = ?2, trade_location = ST_GeomFromText(?3, 4326), updated_at = NOW() 
            WHERE id = ?1
            """, nativeQuery = true)
    void updateTradeLocationAsNativeQuery(Long userId, String tradeAddress, String tradeLocation);
}
