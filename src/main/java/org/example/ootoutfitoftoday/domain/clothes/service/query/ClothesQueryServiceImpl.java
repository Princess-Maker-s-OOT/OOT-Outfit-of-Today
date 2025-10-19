package org.example.ootoutfitoftoday.domain.clothes.service.query;

import lombok.RequiredArgsConstructor;
import org.example.ootoutfitoftoday.domain.clothes.dto.response.ClothesResponse;
import org.example.ootoutfitoftoday.domain.clothes.entity.Clothes;
import org.example.ootoutfitoftoday.domain.clothes.enums.ClothesColor;
import org.example.ootoutfitoftoday.domain.clothes.enums.ClothesSize;
import org.example.ootoutfitoftoday.domain.clothes.repository.ClothesRepository;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ClothesQueryServiceImpl implements ClothesQueryService {

    private final ClothesRepository clothesRepository;

    public Page<ClothesResponse> getClothes(
            Long categoryId,
            Long userId,
            ClothesColor clothesColor,
            ClothesSize clothesSize,
            int page,
            int size,
            String sort,
            String direction
    ) {

        Page<Clothes> clothes = clothesRepository.findAllByIsDeletedFalse(
                categoryId,
                userId,
                clothesColor,
                clothesSize,
                page,
                size,
                sort,
                direction
        );

        return clothes.map(ClothesResponse::from);
    }
}
