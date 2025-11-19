package org.example.ootoutfitoftoday.domain.user.repository;

import org.example.ootoutfitoftoday.domain.auth.enums.SocialProvider;
import org.example.ootoutfitoftoday.domain.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long>, UserCustomRepository {

    boolean existsByLoginId(String loginId);

    boolean existsByEmail(String email);

    boolean existsByNickname(String nickname);

    boolean existsByPhoneNumber(String phoneNumber);

    Optional<User> findByLoginIdAndIsDeletedFalse(String loginId);

    Optional<User> findByIdAndIsDeletedFalse(Long id);

    Optional<User> findByEmailAndIsDeletedFalse(String email);

    Optional<User> findBySocialProviderAndSocialId(SocialProvider provider, String socialId);

    @Query("""
            SELECT count(u)
            FROM User u
            WHERE u.role = 'ROLE_USER'
            """)
    int countAllUsers();

    @Query("""
            SELECT count(u)
            FROM User u
            WHERE u.role = 'ROLE_USER'
              AND u.isDeleted = :isDeleted
            """)
    int countByIsDeleted(Boolean isDeleted);

    @Query("""
            SELECT count(u)
            FROM User u
            WHERE u.role = 'ROLE_USER'
              AND u.createdAt >= :start
              AND u.createdAt < :end
            """)
    int countUsersRegisteredSince(LocalDateTime start, LocalDateTime end);

    @Query("""
            SELECT u.id
            FROM User u
            WHERE u.isDeleted = false
            ORDER BY u.id
            """)
    Page<Long> findAllActiveUserIds(org.springframework.data.domain.Pageable pageable);

    @Modifying
    @Transactional
    @Query(value = """
            INSERT INTO users 
                (login_id, email, nickname, username, password, 
                 phone_number, role, trade_address, trade_location, 
                             image_url, is_deleted, created_at, updated_at)
            VALUES (?1, ?2, ?3, ?4, ?5, ?6, ?7, ?8, ST_GeomFromText(?9, 4326), ?10, ?11, NOW(), NOW())
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

    @Query(value = """
            SELECT u.id,
                u.login_id,
                u.email, 
                u.nickname, 
                u.username, 
                u.password, 
                u.phone_number, 
                u.role, 
                u.trade_address, 
                ST_AsText(u.trade_location) AS trade_location, 
                u.image_url, 
                u.user_image_id,
                u.login_type,
                u.social_provider,   
                u.social_id,
                u.created_at, 
                u.updated_at, 
                u.is_deleted, 
                u.deleted_at 
            FROM users u
            LEFT JOIN user_images ui ON u.user_image_id = ui.id
            WHERE u.id = ?1 AND u.is_deleted = FALSE
            """, nativeQuery = true
    )
    User findByIdAsNativeQuery(Long userId);
}