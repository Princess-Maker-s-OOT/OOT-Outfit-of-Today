package org.example.ootoutfitoftoday.domain.salepost.service.command;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.ootoutfitoftoday.domain.category.entity.Category;
import org.example.ootoutfitoftoday.domain.category.service.query.CategoryQueryService;
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

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class SalePostCommandServiceImpl implements SalePostCommandService {

    private final UserQueryService userQueryService;
    private final CategoryQueryService categoryQueryService;
    private final SalePostRepository salePostRepository;

    @Override
    public SalePostCreateResponse createSalePost(
            Long userId,
            SalePostCreateRequest request,
            List<String> imageUrls
    ) {
        User user = userQueryService.findByIdAndIsDeletedFalse(userId);

        Category category = categoryQueryService.findById(request.getCategoryId());

        SalePost salePost = SalePost.create(
                user,
                category,
                request.getTitle(),
                request.getContent(),
                request.getPrice(),
                imageUrls
        );

        SalePost savedSalePost = salePostRepository.save(salePost);

        return SalePostCreateResponse.from(savedSalePost);
    }

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

        if (salePost.getStatus() == SaleStatus.RESERVED || salePost.getStatus() == SaleStatus.SOLD) {
            log.warn("Cannot update sale post - salePostId: {}, status: {}",
                    salePostId, salePost.getStatus());
            throw new SalePostException(SalePostErrorCode.CANNOT_UPDATE_NON_SELLING_POST);
        }

        Category category = categoryQueryService.findById(request.getCategoryId());

        salePost.update(
                category,
                request.getTitle(),
                request.getContent(),
                request.getPrice(),
                request.getImageUrls()
        );

        return SalePostDetailResponse.from(salePost);
    }

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

        return SalePostDetailResponse.from(salePost);
    }
}
