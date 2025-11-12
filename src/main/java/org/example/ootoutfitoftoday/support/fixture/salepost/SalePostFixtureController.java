package org.example.ootoutfitoftoday.support.fixture.salepost;

import lombok.RequiredArgsConstructor;
import org.example.ootoutfitoftoday.common.response.Response;
import org.example.ootoutfitoftoday.domain.auth.dto.AuthUser;
import org.example.ootoutfitoftoday.domain.salepost.exception.SalePostSuccessCode;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 판매글 테스트 데이터 생성 컨트롤러
 * 성능 테스트 및 개발 환경에서만 활성화
 */
@Profile({"local", "dev"})
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/fixture/sale-posts")
public class SalePostFixtureController {

    private final SalePostFixtureService salePostFixtureService;

    /**
     * 성능 테스트용 대량 더미 데이터 생성
     * 개발 환경에서만 사용
     */
    @PostMapping("/test-data")
    public ResponseEntity<Response<Void>> generateTestData(
            @AuthenticationPrincipal AuthUser authUser,
            @RequestParam int count
    ) {

        salePostFixtureService.generateTestData(authUser.getUserId(), count);

        return Response.success(null, SalePostSuccessCode.SALE_POST_CREATED);
    }
}
