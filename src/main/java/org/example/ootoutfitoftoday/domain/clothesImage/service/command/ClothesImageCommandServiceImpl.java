package org.example.ootoutfitoftoday.domain.clothesImage.service.command;

import lombok.RequiredArgsConstructor;
import org.example.ootoutfitoftoday.domain.clothes.entity.Clothes;
import org.example.ootoutfitoftoday.domain.clothesImage.entity.ClothesImage;
import org.example.ootoutfitoftoday.domain.clothesImage.repository.ClothesImageRepository;
import org.example.ootoutfitoftoday.domain.image.entity.Image;
import org.example.ootoutfitoftoday.domain.image.service.query.ImageQueryService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ClothesImageCommandServiceImpl implements ClothesImageCommandService {

    private final ClothesImageRepository clothesImageRepository;
    private final ImageQueryService imageQueryService;

    @Override
    public void saveClothesImages(Clothes clothes, List<Long> imageIds) {

        // 이미지 ID 목록을 통해 실제 이미지 객체들 조회
        List<Image> images = imageQueryService.findAllByIdIn(imageIds);

        // 이미지 객체들을 ClothesImage로 변환
        List<ClothesImage> clothesImages = new ArrayList<>();

        boolean isMainSet = false;

        for (Image image : images) {

            boolean isMain = !isMainSet; // 첫 번째 이미지가 메인 이미지

            isMainSet = true; // 첫 번째 이미지 설정 후, 이후 이미지는 메인 이미지로 설정하지 않음

            clothesImages.add(ClothesImage.create(clothes, image, isMain));
        }

        // ClothesImage 저장
        clothesImageRepository.saveAll(clothesImages);

        // Clothes 객체에 이미지를 추가
        clothes.addImages(clothesImages);
    }
}
