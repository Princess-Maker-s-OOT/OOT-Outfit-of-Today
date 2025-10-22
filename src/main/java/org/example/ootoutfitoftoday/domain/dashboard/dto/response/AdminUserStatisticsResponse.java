package org.example.ootoutfitoftoday.domain.dashboard.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AdminUserStatisticsResponse {
    private final int totalUsers;
    private final int activeUsers;
    private final int deletedUsers;
    private final NewUsers newUsers;

    @Getter
    @AllArgsConstructor
    public static class NewUsers {
        private final int daily;
        private final int weekly;
        private final int monthly;
    }
}



