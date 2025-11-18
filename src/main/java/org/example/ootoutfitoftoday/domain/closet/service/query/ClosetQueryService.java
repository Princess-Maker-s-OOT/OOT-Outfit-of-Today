package org.example.ootoutfitoftoday.domain.closet.service.query;

import org.example.ootoutfitoftoday.domain.closet.dto.response.ClosetGetMyResponse;
import org.example.ootoutfitoftoday.domain.closet.dto.response.ClosetGetPublicResponse;
import org.example.ootoutfitoftoday.domain.closet.dto.response.ClosetGetResponse;
import org.example.ootoutfitoftoday.domain.closet.entity.Closet;
import org.springframework.data.domain.Page;

public interface ClosetQueryService {

    Page<ClosetGetPublicResponse> getPublicClosets(
            int page,
            int size,
            String sort,
            String direction
    );

    ClosetGetResponse getCloset(Long closetId);

    Page<ClosetGetMyResponse> getMyClosets(
            Long userId,
            int page,
            int size,
            String sort,
            String direction
    );

    Closet findClosetById(Long closetId);
}
