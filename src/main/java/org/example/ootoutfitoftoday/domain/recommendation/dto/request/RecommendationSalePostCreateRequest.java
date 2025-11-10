package org.example.ootoutfitoftoday.domain.recommendation.dto.request;

import jakarta.validation.constraints.*;
import lombok.AccessLevel;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.List;

// 추천으로부터 판매글 생성 요청 DTO
@Builder(access = AccessLevel.PRIVATE)
public record RecommendationSalePostCreateRequest(

        @NotBlank(message = "제목은 필수입니다.")
        @Size(max = 100, message = "제목은 100자 이하여야 합니다.")
        String title,

        @NotBlank(message = "내용은 필수입니다.")
        String content,

        @NotNull(message = "가격은 필수입니다.")
        @DecimalMin(value = "0", inclusive = true, message = "가격은 0원 이상이어야 합니다.")
        BigDecimal price,

        @NotNull(message = "카테고리 선택은 필수입니다.")
        Long categoryId,

        @NotNull(message = "주소는 필수입니다.")
        String tradeAddress,

        @NotNull(message = "위도는 필수입니다.")
        @DecimalMin(value = "33.0", message = "위도는 33 이상이어야 합니다.")
        @DecimalMax(value = "38.6", message = "위도는 38.6 이하이어야 합니다.")
        BigDecimal tradeLatitude,

        @NotNull(message = "경도는 필수입니다.")
        @DecimalMin(value = "124.6", message = "경도는 124.6 이상이어야 합니다.")
        @DecimalMax(value = "131.9", message = "경도는 131.9 이하이어야 합니다.")
        BigDecimal tradeLongitude,

        @NotEmpty(message = "이미지는 최소 1개 이상 필요합니다.")
        List<String> imageUrls
) {
    public static RecommendationSalePostCreateRequest of(
            String title,
            String content,
            BigDecimal price,
            Long categoryId,
            String tradeAddress,
            BigDecimal tradeLatitude,
            BigDecimal tradeLongitude,
            List<String> imageUrls
    ) {

        return RecommendationSalePostCreateRequest.builder()
                .title(title)
                .content(content)
                .price(price)
                .categoryId(categoryId)
                .tradeAddress(tradeAddress)
                .tradeLatitude(tradeLatitude)
                .tradeLongitude(tradeLongitude)
                .imageUrls(imageUrls)
                .build();
    }
}