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
    @DecimalMin(value = "0", inclusive = false, message = "가격은 0보다 커야 합니다.")
    private BigDecimal price;

    @NotNull(message = "카테고리 선택은 필수입니다.")
    private Long categoryId;

    @NotEmpty(message = "이미지는 최소 1개 이상 필요합니다.")
    private List<String> imageUrls;
}
