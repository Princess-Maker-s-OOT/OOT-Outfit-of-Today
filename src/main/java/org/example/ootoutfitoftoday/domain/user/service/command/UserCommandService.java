package org.example.ootoutfitoftoday.domain.user.service.command;

import org.example.ootoutfitoftoday.domain.user.entity.User;

public interface UserCommandService {

    void save(User user);

    void softDeleteUser(User user);
}
