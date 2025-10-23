package org.example.ootoutfitoftoday.domain.dashboard.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.example.ootoutfitoftoday.domain.category.dto.response.CategoryStat;
import org.example.ootoutfitoftoday.domain.clothes.dto.response.ClothesColorCount;
import org.example.ootoutfitoftoday.domain.clothes.dto.response.ClothesSizeCount;

import java.util.List;

@Getter
@AllArgsConstructor
public class AdminClothesStatisticsResponse {

    private final long totalClothes;
    private final List<CategoryStat> categoryStats;
    private final List<ClothesColorCount> colorStats;
    private final List<ClothesSizeCount> sizeStats;
}
