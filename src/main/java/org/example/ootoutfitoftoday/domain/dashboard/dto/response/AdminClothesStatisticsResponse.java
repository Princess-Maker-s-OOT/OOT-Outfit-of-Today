package org.example.ootoutfitoftoday.domain.dashboard.dto.response;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import org.example.ootoutfitoftoday.domain.category.dto.response.CategoryStat;
import org.example.ootoutfitoftoday.domain.clothes.dto.response.ClothesColorCount;
import org.example.ootoutfitoftoday.domain.clothes.dto.response.ClothesSizeCount;

import java.io.Serializable;
import java.util.List;

@Getter
public class AdminClothesStatisticsResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    private final long totalClothes;
    private final List<CategoryStat> categoryStats;
    private final List<ClothesColorCount> colorStats;
    private final List<ClothesSizeCount> sizeStats;

    @JsonCreator
    public AdminClothesStatisticsResponse(
            @JsonProperty("totalClothes") long totalClothes,
            @JsonProperty("categoryStats") List<CategoryStat> categoryStats,
            @JsonProperty("colorStats") List<ClothesColorCount> colorStats,
            @JsonProperty("sizeStats") List<ClothesSizeCount> sizeStats
    ) {
        this.totalClothes = totalClothes;
        this.categoryStats = categoryStats;
        this.colorStats = colorStats;
        this.sizeStats = sizeStats;
    }

}
