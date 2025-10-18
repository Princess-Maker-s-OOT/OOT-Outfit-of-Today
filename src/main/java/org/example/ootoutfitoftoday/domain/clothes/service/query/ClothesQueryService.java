package org.example.ootoutfitoftoday.domain.clothes.service.query;

import org.example.ootoutfitoftoday.domain.clothes.dto.response.ClothesResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ClothesQueryService {

    Page<ClothesResponse> getClothes(Pageable pageable);
}
