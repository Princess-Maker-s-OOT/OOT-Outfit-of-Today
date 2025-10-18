package org.example.ootoutfitoftoday.domain.clothes.service.command;

import org.example.ootoutfitoftoday.domain.clothes.dto.request.ClothesRequest;
import org.example.ootoutfitoftoday.domain.clothes.dto.response.ClothesResponse;

public interface ClothesCommandService {

    ClothesResponse createClothes(ClothesRequest clothesRequest);
}
