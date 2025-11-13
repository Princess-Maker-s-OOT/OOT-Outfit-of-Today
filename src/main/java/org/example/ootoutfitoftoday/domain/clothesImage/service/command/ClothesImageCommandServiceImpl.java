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

import java.util.*;
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

        // 이미 다른 곳에서 이미지를 사용하고 있다면
        if (clothesImageRepository.existsLinkedImages(clothes.getId(), imageIds)) {
            log.warn("saveClothesImages - 이미 다른 곳과 링크된 이미지 시도. clothesId={}, imageIds={}", clothes.getId(), imageIds);
            throw new ClothesImageException(ClothesImageErrorCode.IMAGE_ALREADY_LINKED);
        }

        // 이미지 ID 목록을 통해 실제 이미지 객체들 조회
        List<Image> images = imageQueryService.findAllByIdInAndIsDeletedFalse(imageIds);

        /**
         * images.stream() -> DB에서 가져온 List<Image>를 Stream으로 순회
         * .collect(Collectors.toMap(...)) -> 각 Image의 id를 key로, Image 객체 자체를 value로 Map으로 변환
         * Image::getId -> key: 이미지의 PK (Long)
         * img -> img -> value: 이미지 엔티티 자체
         */
        // 사용자가 입력한 이미지 순서 보장
        Map<Long, Image> imageMap = images.stream()
                .collect(Collectors.toMap(Image::getId, img -> img));

        /**
         * imageIds.stream() -> 사용자가 요청한 이미지 ID 목록 (예: [3, 1, 2])을 순회
         * .map(imageMap::get) -> 각 ID로 imageMap에서 Image 객체를 찾아 반환 (순서 유지)
         * .filter(Objects::nonNull) -> 혹시 DB에 존재하지 않는 ID가 있으면 제외
         * .toList() -> 순서가 보장된 List<Image> 생성
         */
        // 순서 재정렬
        List<Image> orderedImages = imageIds.stream()
                .map(imageMap::get)
                .filter(Objects::nonNull)
                .toList();

        // 이미지 객체들을 ClothesImage로 변환
        List<ClothesImage> clothesImages = new ArrayList<>();

        boolean isMainSet = false;

        for (Image image : orderedImages) {

            boolean isMain = !isMainSet; // 첫 번째 이미지가 메인 이미지

            isMainSet = true; // 첫 번째 이미지 설정 후, 이후 이미지는 메인 이미지로 설정하지 않음

            clothesImages.add(ClothesImage.create(clothes, image, isMain));
        }

        // ClothesImage 저장
        clothesImageRepository.saveAll(clothesImages);

        // Clothes 객체에 이미지를 추가
        clothes.addImages(clothesImages);
    }

    @Override
    public void updateClothesImages(Clothes clothes, List<Long> newImageIds) {

        // 이미지 중복 방지
        if (clothesImageRepository.existsLinkedImages(clothes.getId(), newImageIds)) {
            log.warn("updateClothesImages - 이미 다른 곳에 연결된 이미지 시도. clothesId={}, imageIds={}", clothes.getId(), newImageIds);
            throw new ClothesImageException(ClothesImageErrorCode.IMAGE_ALREADY_LINKED);
        }

        // 기존 이미지 목록 조회 (삭제되지 않은 데이터만 조회)
        List<ClothesImage> existingImages = clothesImageRepository.findByClothesId(clothes.getId());
        List<Long> existingImageIds = existingImages.stream()
                .map(ci -> ci.getImage().getId())
                .toList();

        // 삭제해야 할 이미지 ID 목록
        List<Long> toDeleteIds = existingImageIds.stream()
                .filter(oldId -> !newImageIds.contains(oldId))
                .toList();

        // 새로 추가해야 할 이미지 ID목록
        List<Long> toAddIds = newImageIds.stream()
                .filter(newId -> !existingImageIds.contains(newId))
                .toList();

        // 삭제 처리 (Soft Delete)
        if (!toDeleteIds.isEmpty()) {

            List<ClothesImage> toDelete = existingImages.stream()
                    .filter(ci -> toDeleteIds.contains(ci.getImage().getId()))
                    .toList();

            toDelete.forEach(ClothesImage::softDelete);
            clothesImageRepository.saveAll(toDelete);
        }

        // 복원 처리 (기존에 soft delete 되어있던 옷-이미지 상태변환)
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

        // 복원된 이미지 제외 추가할 이미지
        List<Long> newIds = toAddIds.stream()
                .filter(id -> !restoredIds.contains(id))
                .toList();

        // 새로운 이미지 추가 처리
        if (!newIds.isEmpty()) {

            List<Image> newImages = imageQueryService.findAllByIdInAndIsDeletedFalse(newIds);

            // 순서 보장
            Map<Long, Image> imageMap = newImages.stream()
                    .collect(Collectors.toMap(Image::getId, img -> img));

            List<Image> orderedImages = newIds.stream()
                    .map(imageMap::get)
                    .filter(Objects::nonNull)
                    .toList();

            Long firstImageId = newImageIds.get(0); // 요청의 첫 번째 이미지 = 메인 이미지

            List<ClothesImage> newClothesImages = orderedImages.stream()
                    .map(image -> ClothesImage.create(clothes, image, image.getId().equals(firstImageId)))
                    .toList();

            clothesImageRepository.saveAll(newClothesImages);
        }

        List<ClothesImage> refreshed = clothesImageRepository.findByClothesId(clothes.getId());
        clothes.addImages(refreshed);

        // main 이미지 재설정
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

        // 삭제되지 않은 옷-이미지 조회
        List<ClothesImage> linkedImages = clothesImageRepository.findByClothesIdAndImageIdsAndIsDeletedFalse(clothesId, imageIds);

        // 연관관계가 없다면
        if (linkedImages.isEmpty()) {
            log.warn("removeClothesImages - 연관관계 없는 이미지 삭제 시도. clothesId={}, imageIds={}", clothesId, imageIds);
            throw new ClothesImageException(ClothesImageErrorCode.CLOTHES_IMAGE_NOT_FOUND);
        }

        boolean mainImageWillBeDeleted = linkedImages.stream()
                .anyMatch(ClothesImage::getIsMain);

        // softDelete 처리
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
