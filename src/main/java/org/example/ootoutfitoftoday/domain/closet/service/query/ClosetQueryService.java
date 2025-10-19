package org.example.ootoutfitoftoday.domain.closet.service.query;

import org.example.ootoutfitoftoday.domain.closet.dto.response.ClosetGetPublicResponse;
import org.springframework.data.domain.Page;

public interface ClosetQueryService {

    Page<ClosetGetPublicResponse> getPublicClosets(int page, int size, String sort, String direction);
}
