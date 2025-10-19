package org.example.ootoutfitoftoday.domain.closet.service.query;

import lombok.RequiredArgsConstructor;
import org.example.ootoutfitoftoday.domain.closet.dto.response.ClosetGetPublicResponse;
import org.example.ootoutfitoftoday.domain.closet.entity.Closet;
import org.example.ootoutfitoftoday.domain.closet.repository.ClosetRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClosetQueryServiceImpl implements ClosetQueryService {

    private final ClosetRepository closetRepository;

    // 공개 옷장 리스트 조회
    public Page<ClosetGetPublicResponse> getPublicClosets(
            int page,
            int size,
            String sort,
            String direction
    ) {
        Sort sortObj = Sort.by(Sort.Direction.fromString(direction), sort);

        Pageable pageable = PageRequest.of(page, size, sortObj);

        Page<Closet> closets = closetRepository.findAllByIsPublicTrueAndIsDeletedFalse(pageable);

        return closets.map(ClosetGetPublicResponse::from);
    }
}
