package org.example.ootoutfitoftoday.support.fixture.salepost;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.ootoutfitoftoday.common.util.DefaultLocationConstants;
import org.example.ootoutfitoftoday.domain.category.entity.Category;
import org.example.ootoutfitoftoday.domain.category.service.query.CategoryQueryService;
import org.example.ootoutfitoftoday.domain.salepost.repository.SalePostRepository;
import org.example.ootoutfitoftoday.domain.user.entity.User;
import org.example.ootoutfitoftoday.domain.user.service.query.UserQueryService;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * 매치 테스트 데이터 생성 서비스
 * 성능 테스트 및 개발 환경에서 대량의 더미 데이터를 생성하기 위한 용도
 */
@Profile({"local","dev"})
@Slf4j
@Service
@RequiredArgsConstructor
public class SalePostFixtureService {

    private final UserQueryService userQueryService;
    private final CategoryQueryService categoryQueryService;
    private final SalePostRepository salePostRepository;

    @Transactional
    public void generateTestData(Long userId, int count) {

        User user = userQueryService.findByIdAsNativeQuery(userId);
        Category category = categoryQueryService.findById(SalePostFixtureData.CATEGORY_ID);

        for(int i = 0; i < count; i++){

            salePostRepository.saveAsNativeQuery(
                    SalePostFixtureData.TITLE + " #" + (i + 1),
                    SalePostFixtureData.CONTENT,
                    SalePostFixtureData.PRICE,
                    SalePostFixtureData.SALE_STATUS,
                    DefaultLocationConstants.DEFAULT_TRADE_ADDRESS,
                    DefaultLocationConstants.DEFAULT_TRADE_LOCATION,
                    userId,
                    SalePostFixtureData.CATEGORY_ID,
                    null,
                    false
            );
        }
    }
}
