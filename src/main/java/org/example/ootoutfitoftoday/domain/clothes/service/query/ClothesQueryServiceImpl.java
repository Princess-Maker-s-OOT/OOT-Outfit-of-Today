package org.example.ootoutfitoftoday.domain.clothes.service.query;

import com.ootcommon.category.response.CategoryStat;
import com.ootcommon.clothes.enums.ClothesColor;
import com.ootcommon.clothes.enums.ClothesSize;
import com.ootcommon.clothes.response.ClothesColorCount;
import com.ootcommon.clothes.response.ClothesSizeCount;
import com.ootcommon.wearrecord.response.ClothesWearCount;
import com.ootcommon.wearrecord.response.NotWornOverPeriod;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.ootoutfitoftoday.domain.clothes.dto.response.ClothesResponse;
import org.example.ootoutfitoftoday.domain.clothes.entity.Clothes;
import org.example.ootoutfitoftoday.domain.clothes.exception.ClothesErrorCode;
import org.example.ootoutfitoftoday.domain.clothes.exception.ClothesException;
import org.example.ootoutfitoftoday.domain.clothes.repository.ClothesRepository;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ClothesQueryServiceImpl implements ClothesQueryService {

    private final ClothesRepository clothesRepository;

    @Override
    public Slice<ClothesResponse> getClothes(
            Long userId,
            Long categoryId,
            ClothesColor clothesColor,
            ClothesSize clothesSize,
            Long lastClothesId, // 커서 기준 (무한스크롤용)
            int size
    ) {

        Slice<Clothes> clothesSlice = clothesRepository.findAllByIsDeletedFalse(
                userId,
                categoryId,
                clothesColor,
                clothesSize,
                lastClothesId,
                size
        );

        List<ClothesResponse> clothesResponses = clothesSlice.getContent().stream()
                .map(ClothesResponse::from)
                .collect(Collectors.toList());

        return new SliceImpl<>(
                clothesResponses,
                clothesSlice.getPageable(),
                clothesSlice.hasNext()
        );
    }

    @Override
    public ClothesResponse getClothesById(Long userId, Long id) {

        Clothes clothes = clothesRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> {
                            log.warn("getClothesById - 옷 없음. id={}", id);

                            return new ClothesException(ClothesErrorCode.CLOTHES_NOT_FOUND);
                        }
                );

        if (!Objects.equals(userId, clothes.getUser().getId())) {
            log.warn("getClothesById - 권한 없는 접근! userId={}, clothesOwnerId={}, clothesId={}",
                    userId,
                    clothes.getUser().getId(),
                    id
            );
            throw new ClothesException(ClothesErrorCode.CLOTHES_FORBIDDEN);
        }

        return ClothesResponse.from(clothes);
    }

    @Override
    public Clothes findClothesById(Long id) {

        return clothesRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> {
                            log.warn("findClothesById - 옷 없음. id={}", id);

                            return new ClothesException(ClothesErrorCode.CLOTHES_NOT_FOUND);
                        }
                );
    }

    @Override
    public int countClothesByIsDeletedFalse() {

        return clothesRepository.countAllClothesByIsDeletedFalse();
    }

    @Override
    public List<CategoryStat> countTopCategoryStats() {

        return clothesRepository.countTopCategoryStats();
    }

    @Override
    public List<ClothesColorCount> clothesColorsCount() {

        return clothesRepository.clothesColorsCount();
    }

    @Override
    public List<ClothesSizeCount> clothesSizesCount() {

        return clothesRepository.clothesSizesCount();
    }

    @Override
    public List<CategoryStat> findTopCategoryStats() {

        return clothesRepository.findTopCategoryStats();
    }

    @Override
    public int countAllClothesByUserIdAndIsDeletedFalse(Long userId) {

        return clothesRepository.countAllClothesByUserIdAndIsDeletedFalse(userId);
    }

    @Override
    public List<CategoryStat> countUserTopCategoryStats(Long userId) {

        return clothesRepository.countUserTopCategoryStats(userId);
    }

    @Override
    public List<Clothes> findAllClothesByUserId(Long userId) {

        return clothesRepository.findAllByUserIdAndIsDeletedFalse(userId);
    }

    @Override
    public List<ClothesWearCount> leastWornClothes(Long userId) {

        return clothesRepository.leastWornClothes(userId);
    }

    // Todo: notWornOverPeriod 메소드에서 lastWornAt이 null인 경우(한 번도 입지 않은 옷) daysNotWorn을 0L로 설정하고 있습니다. 이는 '0일 전에 입었다'는 의미로 해석될 수 있어 사용자에게 혼란을 줄 수 있습니다. 한 번도 입지 않은 경우, daysNotWorn을 null로 두거나, 아주 큰 값을 설정하거나, 혹은 UI에서 별도로 "착용 기록 없음" 등으로 표시하는 것이 더 명확할 것 같습니다.
    @Override
    public List<NotWornOverPeriod> notWornOverPeriod(Long userId) {

        List<NotWornOverPeriod> result = clothesRepository.notWornOverPeriod(userId)
                .stream()
                .map(dto -> NotWornOverPeriod.builder()
                        .clothesId(dto.getClothesId())
                        .clothesDescription(dto.getClothesDescription())
                        .lastWornAt(dto.getLastWornAt())
                        .daysNotWorn(dto.getLastWornAt() == null
                                ? 0L
                                : ChronoUnit.DAYS.between(dto.getLastWornAt(), LocalDateTime.now()))
                        .build())
                .toList();

        return result;
    }
}