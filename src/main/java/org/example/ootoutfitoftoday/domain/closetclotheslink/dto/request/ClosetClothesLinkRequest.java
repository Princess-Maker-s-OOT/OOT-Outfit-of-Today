package org.example.ootoutfitoftoday.domain.closetclotheslink.dto.request;

import jakarta.validation.constraints.NotNull;

public record ClosetClothesLinkRequest(

        @NotNull(message = "옷 ID는 필수입니다.")
        Long clothesId
) {
}