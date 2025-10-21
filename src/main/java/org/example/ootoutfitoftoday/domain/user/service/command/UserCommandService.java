package org.example.ootoutfitoftoday.domain.user.service.command;

import org.example.ootoutfitoftoday.domain.auth.dto.AuthUser;
import org.example.ootoutfitoftoday.domain.user.dto.request.UserUpdateInfoRequest;
import org.example.ootoutfitoftoday.domain.user.dto.response.GetMyInfoResponse;
import org.example.ootoutfitoftoday.domain.user.entity.User;

public interface UserCommandService {

    void save(User user);

    void softDeleteUser(User user);

    GetMyInfoResponse updateMyInfo(UserUpdateInfoRequest request, AuthUser authUser);
}
