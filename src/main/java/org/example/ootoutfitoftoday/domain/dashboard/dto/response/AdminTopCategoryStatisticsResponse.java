package org.example.ootoutfitoftoday.domain.dashboard.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.example.ootoutfitoftoday.domain.category.dto.response.CategoryStat;

import java.util.List;

@Getter
@AllArgsConstructor
public class AdminTopCategoryStatisticsResponse {

    private final List<CategoryStat> categoryStats;
}
