package org.example.ootoutfitoftoday.domain.clothesImage.service.command;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.ootoutfitoftoday.domain.clothes.entity.Clothes;
import org.example.ootoutfitoftoday.domain.clothesImage.entity.ClothesImage;
import org.example.ootoutfitoftoday.domain.clothesImage.exception.ClothesImageErrorCode;
import org.example.ootoutfitoftoday.domain.clothesImage.exception.ClothesImageException;
import org.example.ootoutfitoftoday.domain.clothesImage.repository.ClothesImageRepository;
import org.example.ootoutfitoftoday.domain.image.entity.Image;
import org.example.ootoutfitoftoday.domain.image.service.query.ImageQueryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ClothesImageCommandServiceImpl implements ClothesImageCommandService {

    private final ClothesImageRepository clothesImageRepository;
    private final ImageQueryService imageQueryService;

    @Override
    public void saveClothesImages(Clothes clothes, List<Long> imageIds) {
        if (clothesImageRepository.existsLinkedImages(clothes.getId(), imageIds)) {
            log.warn("saveClothesImages - 이미 다른 곳과 링크된 이미지 시도. clothesId={}, imageIds={}", clothes.getId(), imageIds);
            throw new ClothesImageException(ClothesImageErrorCode.IMAGE_ALREADY_LINKED);
        }

        List<Image> images = imageQueryService.findAllByIdInAndIsDeletedFalse(imageIds);

        Map<Long, Image> imageMap = images.stream()
                .collect(Collectors.toMap(Image::getId, img -> img));

        List<Image> orderedImages = imageIds.stream()
                .map(imageMap::get)
                .filter(Objects::nonNull)
                .toList();

        List<ClothesImage> clothesImages = new ArrayList<>();

        boolean isMainSet = false;

        for (Image image : orderedImages) {
            boolean isMain = !isMainSet;
            isMainSet = true;
            clothesImages.add(ClothesImage.create(clothes, image, isMain));
        }

        clothesImageRepository.saveAll(clothesImages);

        clothes.addImages(clothesImages);
    }

    @Override
    public void updateClothesImages(Clothes clothes, List<Long> newImageIds) {
        if (clothesImageRepository.existsLinkedImages(clothes.getId(), newImageIds)) {
            log.warn("updateClothesImages - 이미 다른 곳에 연결된 이미지 시도. clothesId={}, imageIds={}", clothes.getId(), newImageIds);
            throw new ClothesImageException(ClothesImageErrorCode.IMAGE_ALREADY_LINKED);
        }

        List<ClothesImage> existingImages = clothesImageRepository.findByClothesId(clothes.getId());
        List<Long> existingImageIds = existingImages.stream()
                .map(ci -> ci.getImage().getId())
                .toList();

        List<Long> toDeleteIds = existingImageIds.stream()
                .filter(oldId -> !newImageIds.contains(oldId))
                .toList();

        List<Long> toAddIds = newImageIds.stream()
                .filter(newId -> !existingImageIds.contains(newId))
                .toList();

        if (!toDeleteIds.isEmpty()) {
            List<ClothesImage> toDelete = existingImages.stream()
                    .filter(ci -> toDeleteIds.contains(ci.getImage().getId()))
                    .toList();

            toDelete.forEach(ClothesImage::softDelete);
            clothesImageRepository.saveAll(toDelete);
        }

        List<ClothesImage> deletedLinks = clothesImageRepository.findDeletedByClothesIdAndImageIds(clothes.getId(), toAddIds);
        List<Long> restoredIds = new ArrayList<>();

        if (!deletedLinks.isEmpty()) {
            deletedLinks.forEach(ClothesImage::restore);

            clothesImageRepository.saveAll(deletedLinks);

            restoredIds.addAll(
                    deletedLinks.stream()
                            .map(ci -> ci.getImage().getId())
                            .toList()
            );
        }

        List<Long> newIds = toAddIds.stream()
                .filter(id -> !restoredIds.contains(id))
                .toList();

        if (!newIds.isEmpty()) {
            List<Image> newImages = imageQueryService.findAllByIdInAndIsDeletedFalse(newIds);

            Map<Long, Image> imageMap = newImages.stream()
                    .collect(Collectors.toMap(Image::getId, img -> img));

            List<Image> orderedImages = newIds.stream()
                    .map(imageMap::get)
                    .filter(Objects::nonNull)
                    .toList();

            Long firstImageId = newImageIds.get(0);

            List<ClothesImage> newClothesImages = orderedImages.stream()
                    .map(image -> ClothesImage.create(clothes, image, image.getId().equals(firstImageId)))
                    .toList();

            clothesImageRepository.saveAll(newClothesImages);
        }

        List<ClothesImage> refreshed = clothesImageRepository.findByClothesId(clothes.getId());
        clothes.addImages(refreshed);

        for (ClothesImage img : clothes.getImages()) {
            if (img.isDeleted()) {
                continue;
            }

            boolean shouldBeMain = img.getImage().getId().equals(newImageIds.get(0));
            img.updateMain(shouldBeMain);
        }
    }

    @Override
    public void removeClothesImages(Long clothesId, List<Long> imageIds) {
        List<ClothesImage> linkedImages = clothesImageRepository.findByClothesIdAndImageIdsAndIsDeletedFalse(clothesId, imageIds);

        if (linkedImages.isEmpty()) {
            log.warn("removeClothesImages - 연관관계 없는 이미지 삭제 시도. clothesId={}, imageIds={}", clothesId, imageIds);
            throw new ClothesImageException(ClothesImageErrorCode.CLOTHES_IMAGE_NOT_FOUND);
        }

        boolean mainImageWillBeDeleted = linkedImages.stream()
                .anyMatch(ClothesImage::getIsMain);

        for (ClothesImage ci : linkedImages) {
            ci.softDelete();
        }

        if (mainImageWillBeDeleted) {
            List<ClothesImage> remainingImages = clothesImageRepository.findByClothesIdAndIsDeletedFalseOrderByCreatedAtAsc(clothesId);

            if (!remainingImages.isEmpty()) {
                ClothesImage remainingImage = remainingImages.get(0);

                remainingImage.updateMain(true);
            }
        }
    }

    @Override
    public int softDeleteAllByClothesId(Long id) {

        return clothesImageRepository.softDeleteAllByClothesId(id);
    }
}