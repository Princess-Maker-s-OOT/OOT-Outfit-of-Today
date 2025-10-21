package org.example.ootoutfitoftoday.domain.closetclotheslink.service.query;

import org.example.ootoutfitoftoday.domain.closetclotheslink.dto.response.ClosetClothesLinkGetResponse;
import org.springframework.data.domain.Page;

public interface ClosetClothesLinkQueryService {

    // 옷장에 등록된 옷 목록 조회
    Page<ClosetClothesLinkGetResponse> getClothesInCloset(
            Long userId,
            Long closetId,
            int page,
            int size,
            String sort,
            String direction
    );
}
