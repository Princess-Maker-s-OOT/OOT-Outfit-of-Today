package org.example.ootoutfitoftoday.domain.closetclotheslink.service.command;

import org.example.ootoutfitoftoday.domain.closetclotheslink.dto.request.ClosetClothesLinkRequest;
import org.example.ootoutfitoftoday.domain.closetclotheslink.dto.response.ClosetClothesLinkDeleteResponse;
import org.example.ootoutfitoftoday.domain.closetclotheslink.dto.response.ClosetClothesLinkResponse;

public interface ClosetClothesLinkCommandService {

    // 특정 옷장에 옷 등록
    ClosetClothesLinkResponse createClosetClothesLink(
            Long userId,
            Long closetId,
            ClosetClothesLinkRequest request
    );

    // 특정 옷장에서 옷 제거
    ClosetClothesLinkDeleteResponse deleteClosetClothesLink(
            Long userId,
            Long closetId,
            Long clothesId
    );
}