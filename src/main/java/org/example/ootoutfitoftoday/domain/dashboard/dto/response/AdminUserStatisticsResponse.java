package org.example.ootoutfitoftoday.domain.dashboard.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.example.ootoutfitoftoday.domain.user.dto.response.NewUsers;

@Getter
@AllArgsConstructor
public class AdminUserStatisticsResponse {

    // 서비스 사이즈가 크진 않기에 int 사용 (중소형 서비스)
    private final int totalUsers;
    private final int activeUsers;
    private final int deletedUsers;
    private final NewUsers newUsers;
}



