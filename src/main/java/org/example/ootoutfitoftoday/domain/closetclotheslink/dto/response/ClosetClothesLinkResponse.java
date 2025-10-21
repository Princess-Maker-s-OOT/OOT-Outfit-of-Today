package org.example.ootoutfitoftoday.domain.closetclotheslink.dto.response;

import org.example.ootoutfitoftoday.domain.closetclotheslink.entity.ClosetClothesLink;

public record ClosetClothesLinkResponse(

        Long linkId,
        Long closetId,
        Long clothesId
) {
    public static ClosetClothesLinkResponse from(ClosetClothesLink link) {

        return new ClosetClothesLinkResponse(
                link.getId(),
                link.getCloset().getId(),
                link.getClothes().getId()
        );
    }
}