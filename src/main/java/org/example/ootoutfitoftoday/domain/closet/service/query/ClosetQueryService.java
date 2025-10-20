package org.example.ootoutfitoftoday.domain.closet.service.query;

import org.example.ootoutfitoftoday.domain.closet.dto.response.ClosetGetMyResponse;
import org.example.ootoutfitoftoday.domain.closet.dto.response.ClosetGetPublicResponse;
import org.example.ootoutfitoftoday.domain.closet.dto.response.ClosetGetResponse;
import org.springframework.data.domain.Page;

public interface ClosetQueryService {

    // 공개 옷장 리스트 조회
    Page<ClosetGetPublicResponse> getPublicClosets(
            int page,
            int size,
            String sort,
            String direction
    );

    // 옷장 상세 조회
    ClosetGetResponse getCloset(Long closetId);

    // 내 옷장 리스트 조회
    Page<ClosetGetMyResponse> getMyClosets(
            Long userId,
            int page,
            int size,
            String sort,
            String direction
    );
}
