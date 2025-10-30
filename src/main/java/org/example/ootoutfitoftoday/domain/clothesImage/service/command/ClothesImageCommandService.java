package org.example.ootoutfitoftoday.domain.clothesImage.service.command;

import org.example.ootoutfitoftoday.domain.clothes.entity.Clothes;

import java.util.List;

public interface ClothesImageCommandService {

    void saveClothesImages(Clothes clothes, List<Long> imageIds);
}
