package org.example.ootoutfitoftoday.domain.closetclotheslink.service.command;

import org.example.ootoutfitoftoday.domain.closetclotheslink.dto.request.ClosetClothesLinkRequest;
import org.example.ootoutfitoftoday.domain.closetclotheslink.dto.response.ClosetClothesLinkResponse;

public interface ClosetClothesLinkCommandService {

    // 옷장에 옷 등록
    ClosetClothesLinkResponse createClosetClothesLink(
            Long userId,
            Long closetId,
            ClosetClothesLinkRequest request
    );
}