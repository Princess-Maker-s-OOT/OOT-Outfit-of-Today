package org.example.ootoutfitoftoday.domain.salepost.service.query;

import lombok.RequiredArgsConstructor;
import org.example.ootoutfitoftoday.domain.salepost.dto.response.SalePostDetailResponse;
import org.example.ootoutfitoftoday.domain.salepost.dto.response.SalePostListResponse;
import org.example.ootoutfitoftoday.domain.salepost.dto.response.SalePostSummaryResponse;
import org.example.ootoutfitoftoday.domain.salepost.entity.SalePost;
import org.example.ootoutfitoftoday.domain.salepost.enums.SaleStatus;
import org.example.ootoutfitoftoday.domain.salepost.exception.SalePostErrorCode;
import org.example.ootoutfitoftoday.domain.salepost.exception.SalePostException;
import org.example.ootoutfitoftoday.domain.salepost.repository.SalePostRepository;
import org.example.ootoutfitoftoday.domain.user.entity.User;
import org.example.ootoutfitoftoday.domain.user.service.query.UserQueryService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SalePostQueryServiceImpl implements SalePostQueryService {

    private final SalePostRepository salePostRepository;
    private final UserQueryService userQueryService;

    @Override
    public SalePost findSalePostById(Long salePostId) {

        return salePostRepository.findByIdAndIsDeletedFalse(salePostId)
                .orElseThrow(() -> new SalePostException(SalePostErrorCode.SALE_POST_NOT_FOUND));
    }

    @Override
    public SalePostDetailResponse getSalePostDetail(Long salePostId) {

        SalePost salePost = salePostRepository.findByIdWithDetailsAndNotDeleted(salePostId)
                .orElseThrow(() -> new SalePostException(SalePostErrorCode.SALE_POST_NOT_FOUND));

        return SalePostDetailResponse.from(salePost);
    }

    @Override
    public Slice<SalePostListResponse> getSalePostList(
            Long categoryId,
            SaleStatus status,
            String keyword,
            Pageable pageable
    ) {
        String normalizedKeyword =
                (keyword == null || keyword.isBlank()) ? null : keyword.trim();

        Slice<SalePost> salePosts = salePostRepository.findAllWithFilters(
                categoryId,
                status,
                normalizedKeyword,
                pageable
        );

        return salePosts.map(SalePostListResponse::from);
    }

    @Override
    public Slice<SalePostSummaryResponse> findMySalePosts(
            Long userId,
            SaleStatus status,
            Pageable pageable
    ) {
        User user = userQueryService.findByIdAndIsDeletedFalse(userId);

        Slice<SalePost> salePosts = salePostRepository.findMyPosts(
                userId,
                status,
                pageable
        );

        return salePosts.map(SalePostSummaryResponse::from);
    }
}
