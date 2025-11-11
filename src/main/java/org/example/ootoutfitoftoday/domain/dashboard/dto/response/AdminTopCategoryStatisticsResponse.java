package org.example.ootoutfitoftoday.domain.dashboard.dto.response;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import org.example.ootoutfitoftoday.domain.category.dto.response.CategoryStat;

import java.io.Serializable;
import java.util.List;

@Getter
public class AdminTopCategoryStatisticsResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    private final List<CategoryStat> categoryStats;

    @JsonCreator
    public AdminTopCategoryStatisticsResponse(
            @JsonProperty("categoryStats") List<CategoryStat> categoryStats
    ) {
        this.categoryStats = categoryStats;
    }
}
