package org.example.ootoutfitoftoday.domain.salepost.service.command;

import com.ootcommon.salepost.enums.SaleStatus;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.ootoutfitoftoday.common.util.PointFormatAndParse;
import org.example.ootoutfitoftoday.domain.category.entity.Category;
import org.example.ootoutfitoftoday.domain.category.service.query.CategoryQueryService;
import org.example.ootoutfitoftoday.domain.image.entity.Image;
import org.example.ootoutfitoftoday.domain.image.service.query.ImageQueryService;
import org.example.ootoutfitoftoday.domain.recommendation.entity.Recommendation;
import org.example.ootoutfitoftoday.domain.salepost.dto.request.SalePostCreateRequest;
import org.example.ootoutfitoftoday.domain.salepost.dto.request.SalePostUpdateRequest;
import org.example.ootoutfitoftoday.domain.salepost.dto.response.SalePostCreateResponse;
import org.example.ootoutfitoftoday.domain.salepost.dto.response.SalePostDetailResponse;
import org.example.ootoutfitoftoday.domain.salepost.entity.SalePost;
import org.example.ootoutfitoftoday.domain.salepost.entity.SalePostImage;
import org.example.ootoutfitoftoday.domain.salepost.exception.SalePostErrorCode;
import org.example.ootoutfitoftoday.domain.salepost.exception.SalePostException;
import org.example.ootoutfitoftoday.domain.salepost.repository.SalePostImageRepository;
import org.example.ootoutfitoftoday.domain.salepost.repository.SalePostRepository;
import org.example.ootoutfitoftoday.domain.user.entity.User;
import org.example.ootoutfitoftoday.domain.user.service.query.UserQueryService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class SalePostCommandServiceImpl implements SalePostCommandService {

    private final UserQueryService userQueryService;
    private final CategoryQueryService categoryQueryService;
    private final SalePostRepository salePostRepository;
    private final EntityManager entityManager;
    private final ImageQueryService imageQueryService;
    private final SalePostImageRepository salePostImageRepository;

    @Override
    @CacheEvict(value = "salePostListCache", allEntries = true)
    public SalePostCreateResponse createSalePost(Long userId, SalePostCreateRequest request) {
        User user = userQueryService.findByIdAndIsDeletedFalse(userId);

        Category category = categoryQueryService.findById(request.getCategoryId());

        List<Image> images = imageQueryService.findAllByIdInAndIsDeletedFalse(
                request.getImageIds()
        );

        Map<Long, Image> imageMap = images.stream()
                .collect(Collectors.toMap(Image::getId, img -> img));

        List<Image> orderedImages = request.getImageIds().stream()
                .map(imageMap::get)
                .filter(Objects::nonNull)
                .toList();

        String tradeLocation = PointFormatAndParse.format(
                request.getTradeLatitude(),
                request.getTradeLongitude()
        );

        SalePost salePost = SalePost.create(
                user,
                category,
                request.getTitle(),
                request.getContent(),
                request.getPrice(),
                request.getTradeAddress(),
                tradeLocation,
                orderedImages
        );

        return saveSalePostAndCreateResponse(salePost);
    }

    @Override
    public SalePostCreateResponse createSalePostFromRecommendation(
            Recommendation recommendation,
            Long categoryId,
            String title,
            String content,
            BigDecimal price,
            String tradeAddress,
            BigDecimal tradeLatitude,
            BigDecimal tradeLongitude,
            List<Long> imageIds
    ) {
        Category category = categoryQueryService.findById(categoryId);

        List<Image> images = imageQueryService.findAllByIdInAndIsDeletedFalse(imageIds);

        Map<Long, Image> imageMap = images.stream()
                .collect(Collectors.toMap(Image::getId, img -> img));

        List<Image> orderedImages = imageIds.stream()
                .map(imageMap::get)
                .filter(Objects::nonNull)
                .toList();

        String tradeLocation = PointFormatAndParse.format(
                tradeLatitude,
                tradeLongitude
        );

        SalePost salePost = SalePost.createFromRecommendation(
                recommendation,
                category,
                title,
                content,
                price,
                tradeAddress,
                tradeLocation,
                orderedImages
        );

        SalePostCreateResponse response = saveSalePostAndCreateResponse(salePost);

        log.info("Created sale post from recommendation - recommendationId: {}, salePostId: {}",
                recommendation.getId(), response.getSalePostId());

        return response;
    }

    private SalePostCreateResponse saveSalePostAndCreateResponse(SalePost salePost) {
        String status = salePost.getStatus().name();
        Long recommendationId = (salePost.getRecommendation() != null)
                ? salePost.getRecommendation().getId() : null;

        salePostRepository.saveAsNativeQuery(
                salePost.getTitle(),
                salePost.getContent(),
                salePost.getPrice(),
                status,
                salePost.getTradeAddress(),
                salePost.getTradeLocation(),
                salePost.getUser().getId(),
                salePost.getCategory().getId(),
                recommendationId,
                false
        );

        Long salePostId = salePostRepository.findLastInsertId();

        SalePost savedSalePostForImages = salePostRepository.findByIdAsNativeQuery(salePostId)
                .orElseThrow(() -> new SalePostException(SalePostErrorCode.SALE_POST_NOT_FOUND));

        for (SalePostImage image : salePost.getImages()) {
            image.setSalePost(savedSalePostForImages);
        }

        salePostImageRepository.saveAll(salePost.getImages());

        SalePost savedSalePost = salePostRepository.findByIdAsNativeQuery(salePostId)
                .orElseThrow(() -> new SalePostException(SalePostErrorCode.SALE_POST_NOT_FOUND));

        return SalePostCreateResponse.from(savedSalePost);
    }

    @Override
    @CacheEvict(value = "salePostListCache", allEntries = true)
    public SalePostDetailResponse updateSalePost(
            Long salePostId,
            Long userId,
            SalePostUpdateRequest request
    ) {
        SalePost salePost = salePostRepository.findByIdWithDetailsAndNotDeleted(salePostId)
                .orElseThrow(() -> new SalePostException(SalePostErrorCode.SALE_POST_NOT_FOUND));

        if (!salePost.isOwnedBy(userId)) {
            log.warn("Unauthorized access attempt to salePostId: {} by userId: {}", salePostId, userId);
            throw new SalePostException(SalePostErrorCode.UNAUTHORIZED_ACCESS);
        }

        if (salePost.getStatus() == SaleStatus.RESERVED ||
                salePost.getStatus() == SaleStatus.TRADING ||
                salePost.getStatus() == SaleStatus.COMPLETED
        ) {
            log.warn("Cannot update sale post - salePostId: {}, status: {}",
                    salePostId, salePost.getStatus());
            throw new SalePostException(SalePostErrorCode.CANNOT_UPDATE_NON_SELLING_POST);
        }

        String tradeLocation = PointFormatAndParse.format(request.getTradeLatitude(), request.getTradeLongitude());

        log.info("tradeLocation: {}", tradeLocation);

        salePostRepository.updateAsNativeQuery(
                salePostId,
                request.getTitle(),
                request.getContent(),
                request.getPrice(),
                request.getCategoryId(),
                request.getTradeAddress(),
                tradeLocation
        );

        List<SalePostImage> existingImages = salePostImageRepository.findBySalePostId(salePostId);
        for (SalePostImage existingImage : existingImages) {
            existingImage.softDelete();
        }
        salePostImageRepository.saveAll(existingImages);

        List<Image> images = imageQueryService.findAllByIdInAndIsDeletedFalse(request.getImageIds());

        Map<Long, Image> imageMap = images.stream()
                .collect(Collectors.toMap(Image::getId, img -> img));

        List<Image> orderedImages = request.getImageIds().stream()
                .map(imageMap::get)
                .filter(Objects::nonNull)
                .toList();

        SalePost savedSalePost = salePostRepository.findByIdAsNativeQuery(salePostId)
                .orElseThrow(() -> new SalePostException(SalePostErrorCode.SALE_POST_NOT_FOUND));

        List<SalePostImage> newImages = new ArrayList<>();
        for (int i = 0; i < orderedImages.size(); i++) {
            boolean isMain = (i == 0);
            SalePostImage newImage = SalePostImage.create(
                    orderedImages.get(i),
                    i + 1,
                    isMain
            );
            newImage.setSalePost(savedSalePost);
            newImages.add(newImage);
        }

        salePostImageRepository.saveAll(newImages);

        entityManager.clear();

        SalePost updatedSalePost = salePostRepository.findByIdAsNativeQuery(salePostId)
                .orElseThrow(() -> new SalePostException(SalePostErrorCode.SALE_POST_NOT_FOUND));

        return SalePostDetailResponse.from(updatedSalePost);
    }

    @Override
    @CacheEvict(value = "salePostListCache", allEntries = true)
    public void deleteSalePost(Long salePostId, Long userId) {
        SalePost salePost = salePostRepository.findByIdAndIsDeletedFalse(salePostId)
                .orElseThrow(() -> new SalePostException(SalePostErrorCode.SALE_POST_NOT_FOUND));

        if (!salePost.isOwnedBy(userId)) {
            log.warn("Unauthorized delete attempt to salePostId: {} by userId: {}", salePostId, userId);
            throw new SalePostException(SalePostErrorCode.UNAUTHORIZED_ACCESS);
        }

        if (salePost.getStatus() == SaleStatus.RESERVED) {
            log.warn("Cannot delete reserved sale post - salePostId: {}, status: {}",
                    salePostId, salePost.getStatus());
            throw new SalePostException(SalePostErrorCode.CANNOT_DELETE_RESERVED_POST);
        }

        salePost.softDelete();
    }

    @Override
    @CacheEvict(value = "salePostListCache", allEntries = true)
    public SalePostDetailResponse updateSaleStatus(
            Long salePostId,
            Long userId,
            SaleStatus newStatus
    ) {
        SalePost salePost = salePostRepository.findByIdWithDetailsAndNotDeleted(salePostId)
                .orElseThrow(() -> new SalePostException(SalePostErrorCode.SALE_POST_NOT_FOUND));

        if (!salePost.isOwnedBy(userId)) {
            log.warn("Unauthorized status update attempt - salePostId: {} by userId: {}, requestedStatus: {}",
                    salePostId, userId, newStatus);
            throw new SalePostException(SalePostErrorCode.UNAUTHORIZED_ACCESS);
        }

        salePost.updateStatus(newStatus);

        salePostRepository.flush();

        entityManager.clear();

        SalePost updatedSalePost = salePostRepository.findByIdAsNativeQuery(salePostId)
                .orElseThrow(() -> new SalePostException(SalePostErrorCode.SALE_POST_NOT_FOUND));

        return SalePostDetailResponse.from(updatedSalePost);
    }
}