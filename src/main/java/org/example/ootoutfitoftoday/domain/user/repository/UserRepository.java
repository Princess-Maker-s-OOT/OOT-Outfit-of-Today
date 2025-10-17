package org.example.ootoutfitoftoday.domain.user.repository;

import org.example.ootoutfitoftoday.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByLoginId(String loginId);

}
