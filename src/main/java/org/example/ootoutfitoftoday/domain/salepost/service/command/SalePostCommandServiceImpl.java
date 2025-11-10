package org.example.ootoutfitoftoday.domain.salepost.service.command;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.ootoutfitoftoday.common.util.PointFormatAndParse;
import org.example.ootoutfitoftoday.domain.category.entity.Category;
import org.example.ootoutfitoftoday.domain.category.service.query.CategoryQueryService;
import org.example.ootoutfitoftoday.domain.recommendation.entity.Recommendation;
import org.example.ootoutfitoftoday.domain.salepost.dto.request.SalePostCreateRequest;
import org.example.ootoutfitoftoday.domain.salepost.dto.request.SalePostUpdateRequest;
import org.example.ootoutfitoftoday.domain.salepost.dto.response.SalePostCreateResponse;
import org.example.ootoutfitoftoday.domain.salepost.dto.response.SalePostDetailResponse;
import org.example.ootoutfitoftoday.domain.salepost.entity.SalePost;
import org.example.ootoutfitoftoday.domain.salepost.enums.SaleStatus;
import org.example.ootoutfitoftoday.domain.salepost.exception.SalePostErrorCode;
import org.example.ootoutfitoftoday.domain.salepost.exception.SalePostException;
import org.example.ootoutfitoftoday.domain.salepost.repository.SalePostRepository;
import org.example.ootoutfitoftoday.domain.user.entity.User;
import org.example.ootoutfitoftoday.domain.user.service.query.UserQueryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class SalePostCommandServiceImpl implements SalePostCommandService {

    private final UserQueryService userQueryService;
    private final CategoryQueryService categoryQueryService;
    private final SalePostRepository salePostRepository;
    private final EntityManager entityManager;

    // 판매글 생성
    @Override
    public SalePostCreateResponse createSalePost(
            Long userId,
            SalePostCreateRequest request,
            List<String> imageUrls
    ) {
        User user = userQueryService.findByIdAndIsDeletedFalse(userId);

        Category category = categoryQueryService.findById(request.getCategoryId());

        String tradeLocation = PointFormatAndParse.format(request.getTradeLatitude(), request.getTradeLongitude());

        SalePost salePost = SalePost.create(
                user,
                category,
                request.getTitle(),
                request.getContent(),
                request.getPrice(),
                request.getTradeAddress(),
                tradeLocation,
                imageUrls
        );

        return saveSalePostAndCreateResponse(salePost);
    }

    // 추천으로부터 판매글 생성
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
            List<String> imageUrls
    ) {
        Category category = categoryQueryService.findById(categoryId);

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
                imageUrls
        );

        SalePostCreateResponse response = saveSalePostAndCreateResponse(salePost);

        log.info("Created sale post from recommendation - recommendationId: {}, salePostId: {}",
                recommendation.getId(), response.getSalePostId());

        return response;
    }

    // 판매글 저장 및 응답 생성 헬퍼 메서드
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

        SalePost savedSalePost = salePostRepository.findByIdAsNativeQuery(salePostId)
                .orElseThrow(() -> new SalePostException(SalePostErrorCode.SALE_POST_NOT_FOUND));

        return SalePostCreateResponse.from(savedSalePost);
    }

    // 판매글 수정
    @Override
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

        if (salePost.getStatus() == SaleStatus.RESERVED || salePost.getStatus() == SaleStatus.TRADING || salePost.getStatus() == SaleStatus.COMPLETED) {
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

        // 업데이트 이후 영속성 컨텍스트
        entityManager.clear();

        SalePost updatedSalePost = salePostRepository.findByIdAsNativeQuery(salePostId)
                .orElseThrow(() -> new SalePostException(SalePostErrorCode.SALE_POST_NOT_FOUND));

        return SalePostDetailResponse.from(updatedSalePost);
    }

    // 판매글 삭제
    @Override
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

    // 판매글 상태 수정
    @Override
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
