package org.example.ootoutfitoftoday.domain.user.service.query;

public interface UserQueryService {

    boolean existsByLoginId(String loginId);

    boolean existsByEmail(String email);

    boolean existsByNickname(String nickname);

    boolean existsByPhoneNumber(String phoneNumber);
}
