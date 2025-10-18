package org.example.ootoutfitoftoday.domain.clothes.service.query;

import lombok.RequiredArgsConstructor;
import org.example.ootoutfitoftoday.domain.clothes.dto.response.ClothesResponse;
import org.example.ootoutfitoftoday.domain.clothes.entity.Clothes;
import org.example.ootoutfitoftoday.domain.clothes.repository.ClothesRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ClothesQueryServiceImpl implements ClothesQueryService {

    private final ClothesRepository clothesRepository;

    @Override
    public Page<ClothesResponse> getClothes(Pageable pageable) {

        Page<Clothes> clothes = clothesRepository.findAll(pageable);

        return clothes.map(ClothesResponse::from);
    }
}
