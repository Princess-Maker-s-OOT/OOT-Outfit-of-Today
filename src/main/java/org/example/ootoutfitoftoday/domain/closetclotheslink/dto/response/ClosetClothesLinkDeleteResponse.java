package org.example.ootoutfitoftoday.domain.closetclotheslink.dto.response;

public record ClosetClothesLinkDeleteResponse(
        Long closetId,
        Long clothesId
) {

    public static ClosetClothesLinkDeleteResponse of(
            Long closetId,
            Long clothesId
    ) {
    
        return new ClosetClothesLinkDeleteResponse(
                closetId,
                clothesId
        );
    }
}
