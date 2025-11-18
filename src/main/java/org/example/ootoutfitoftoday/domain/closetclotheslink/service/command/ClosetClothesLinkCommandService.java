package org.example.ootoutfitoftoday.domain.closetclotheslink.service.command;

import org.example.ootoutfitoftoday.domain.closetclotheslink.dto.request.ClosetClothesLinkRequest;
import org.example.ootoutfitoftoday.domain.closetclotheslink.dto.response.ClosetClothesLinkDeleteResponse;
import org.example.ootoutfitoftoday.domain.closetclotheslink.dto.response.ClosetClothesLinkResponse;

public interface ClosetClothesLinkCommandService {

    ClosetClothesLinkResponse createClosetClothesLink(
            Long userId,
            Long closetId,
            ClosetClothesLinkRequest request
    );

    ClosetClothesLinkDeleteResponse deleteClosetClothesLink(
            Long userId,
            Long closetId,
            Long clothesId
    );
}