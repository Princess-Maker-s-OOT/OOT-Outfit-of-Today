package org.example.ootoutfitoftoday.domain.salepost.dto.request;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Getter
@NoArgsConstructor
public class SalePostCreateRequest {

    @NotBlank(message = "제목은 필수입니다.")
    @Size(max = 100, message = "제목은 100자 이하여야 합니다.")
    private String title;

    @NotBlank(message = "내용은 필수입니다.")
    private String content;

    @NotNull(message = "가격은 필수입니다.")
    @DecimalMin(value = "0", inclusive = true, message = "가격은 0원 이상이어야 합니다.")
    private BigDecimal price;

    @NotNull(message = "카테고리 선택은 필수입니다.")
    private Long categoryId;

    @NotNull(message = "주소는 필수입니다.")
    private String tradeAddress;

    @NotNull(message = "위도는 필수입니다.")
    @DecimalMin(value = "33.0", message = "위도는 33 이상이어야 합니다.")
    @DecimalMax(value = "38.6", message = "위도는 38.6 이하이어야 합니다.")
    private BigDecimal tradeLatitude;   // 위도

    @NotNull(message = "경도는 필수입니다.")
    @DecimalMin(value = "124.6", message = "경도는 124.6 이상이어야 합니다.")
    @DecimalMax(value = "131.9", message = "경도는 131.9 이하이어야 합니다.")
    private BigDecimal tradeLongitude;  // 경도

    @NotEmpty(message = "이미지는 최소 1개 이상 필요합니다.")
    private List<String> imageUrls;
}
