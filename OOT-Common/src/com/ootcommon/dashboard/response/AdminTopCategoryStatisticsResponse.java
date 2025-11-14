package com.ootcommon.dashboard.response;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.ootcommon.category.response.CategoryStat;
import java.io.Serializable;
import java.util.List;
import lombok.Getter;

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
