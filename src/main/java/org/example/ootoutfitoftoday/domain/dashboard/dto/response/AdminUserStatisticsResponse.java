package org.example.ootoutfitoftoday.domain.dashboard.dto.response;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import org.example.ootoutfitoftoday.domain.user.dto.response.NewUsers;

import java.io.Serializable;

@Getter
public class AdminUserStatisticsResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    // 서비스 사이즈가 크진 않기에 int 사용 (중소형 서비스)
    private final int totalUsers;
    private final int activeUsers;
    private final int deletedUsers;
    private final NewUsers newUsers;

    @JsonCreator
    public AdminUserStatisticsResponse(
            @JsonProperty("totalUsers") int totalUsers,
            @JsonProperty("activeUsers") int activeUsers,
            @JsonProperty("deletedUsers") int deletedUsers,
            @JsonProperty("newUsers") NewUsers newUsers
    ) {
        this.totalUsers = totalUsers;
        this.activeUsers = activeUsers;
        this.deletedUsers = deletedUsers;
        this.newUsers = newUsers;
    }
}



