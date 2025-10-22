package org.example.ootoutfitoftoday.domain.user.repository;

import org.example.ootoutfitoftoday.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long>, CustomUserRepository {

    boolean existsByLoginId(String loginId);

    boolean existsByEmail(String email);

    boolean existsByNickname(String nickname);

    boolean existsByPhoneNumber(String phoneNumber);

    Optional<User> findByLoginIdAndIsDeletedFalse(String loginId);

    Optional<User> findByIdAndIsDeletedFalse(Long id);
}
