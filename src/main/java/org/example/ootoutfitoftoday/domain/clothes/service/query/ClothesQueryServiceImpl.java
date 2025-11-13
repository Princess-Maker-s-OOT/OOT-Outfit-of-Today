package org.example.ootoutfitoftoday.domain.clothes.service.query;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.ootoutfitoftoday.domain.category.dto.response.CategoryStat;
import org.example.ootoutfitoftoday.domain.clothes.dto.response.ClothesColorCount;
import org.example.ootoutfitoftoday.domain.clothes.dto.response.ClothesResponse;
import org.example.ootoutfitoftoday.domain.clothes.dto.response.ClothesSizeCount;
import org.example.ootoutfitoftoday.domain.clothes.entity.Clothes;
import org.example.ootoutfitoftoday.domain.clothes.enums.ClothesColor;
import org.example.ootoutfitoftoday.domain.clothes.enums.ClothesSize;
import org.example.ootoutfitoftoday.domain.clothes.exception.ClothesErrorCode;
import org.example.ootoutfitoftoday.domain.clothes.exception.ClothesException;
import org.example.ootoutfitoftoday.domain.clothes.repository.ClothesRepository;
import org.example.ootoutfitoftoday.domain.wearrecord.dto.response.ClothesWearCount;
import org.example.ootoutfitoftoday.domain.wearrecord.dto.response.NotWornOverPeriod;
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