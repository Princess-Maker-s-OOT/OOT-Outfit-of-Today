package org.example.ootoutfitoftoday.domain.salepost.dto.response;

import lombok.Builder;
import lombok.Getter;
import org.example.ootoutfitoftoday.domain.salepost.entity.SalePostImage;

@Getter
@Builder
public class SalePostImageResponse {
    private final Long imageId;
    private final String imageUrl;
    private final Integer displayOrder;
    private final Boolean isMain;

    public static SalePostImageResponse from(SalePostImage salePostImage) {
        return SalePostImageResponse.builder()
                .imageId(salePostImage.getId())
                .imageUrl(salePostImage.getImageUrl())
                .displayOrder(salePostImage.getDisplayOrder())
                .isMain(salePostImage.getIsMain())
                .build();
    }
}
