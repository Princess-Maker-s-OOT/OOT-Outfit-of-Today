package org.example.ootoutfitoftoday.domain.clothes.service.query;

import com.ootcommon.category.response.CategoryStat;
import com.ootcommon.clothes.enums.ClothesColor;
import com.ootcommon.clothes.enums.ClothesSize;
import com.ootcommon.clothes.response.ClothesColorCount;
import com.ootcommon.clothes.response.ClothesSizeCount;
import com.ootcommon.wearrecord.response.ClothesWearCount;
import com.ootcommon.wearrecord.response.NotWornOverPeriod;
import org.example.ootoutfitoftoday.domain.clothes.dto.response.ClothesResponse;
import org.example.ootoutfitoftoday.domain.clothes.entity.Clothes;
import org.springframework.data.domain.Slice;

import java.util.List;

public interface ClothesQueryService {

    Slice<ClothesResponse> getClothes(
            Long userId,
            Long categoryId,
            ClothesColor clothesColor,
            ClothesSize clothesSize,
            Long lastClothesId, // 커서 기준 (무한스크롤용)
            int size
    );

    ClothesResponse getClothesById(Long userId, Long id);

    Clothes findClothesById(Long id);

    int countClothesByIsDeletedFalse();

    // 최상위 카테고리 기준 옷 통계
    List<CategoryStat> countTopCategoryStats();

    // 색상 기준 옷 통계
    List<ClothesColorCount> clothesColorsCount();

    // 사이즈 기준 옷 통계
    List<ClothesSizeCount> clothesSizesCount();

    // 카테고리 인기 순위
    List<CategoryStat> findTopCategoryStats();

    // 사용자 기준 옷 통계
    int countAllClothesByUserIdAndIsDeletedFalse(Long userId);

    // 사용자 기준 카테고리 옷 통계
    List<CategoryStat> countUserTopCategoryStats(Long userId);

    // 사용자의 모든 옷 엔티티 조회
    List<Clothes> findAllClothesByUserId(Long userId);

    // 자주 입지 않은 옷 (전체 기간)
    List<ClothesWearCount> leastWornClothes(Long userId);

    // 옷 미착용 기간
    List<NotWornOverPeriod> notWornOverPeriod(Long userId);
}