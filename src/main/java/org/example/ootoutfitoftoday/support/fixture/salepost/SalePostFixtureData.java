package org.example.ootoutfitoftoday.support.fixture.salepost;

import org.example.ootoutfitoftoday.domain.salepost.enums.SaleStatus;

import java.math.BigDecimal;

/**
 * 판매글 테스트 데이터 생성을 위한 상수 클래스
 * 성능 테스트 및 개발 환경에서 사용
 */
public class SalePostFixtureData {

    public static final String TITLE = "title";

    public static final String CONTENT = "content";

    public static final String SALE_STATUS = SaleStatus.AVAILABLE.toString();

    public static final BigDecimal PRICE = BigDecimal.valueOf(1000);

    public static final Long CATEGORY_ID = 10L;
}
