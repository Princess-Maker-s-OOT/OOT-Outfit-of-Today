package org.example.ootoutfitoftoday.domain.closet.service.query;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.ootoutfitoftoday.domain.closet.dto.response.ClosetGetMyResponse;
import org.example.ootoutfitoftoday.domain.closet.dto.response.ClosetGetPublicResponse;
import org.example.ootoutfitoftoday.domain.closet.dto.response.ClosetGetResponse;
import org.example.ootoutfitoftoday.domain.closet.entity.Closet;
import org.example.ootoutfitoftoday.domain.closet.exception.ClosetErrorCode;
import org.example.ootoutfitoftoday.domain.closet.exception.ClosetException;
import org.example.ootoutfitoftoday.domain.closet.repository.ClosetRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClosetQueryServiceImpl implements ClosetQueryService {

    private final ClosetRepository closetRepository;

    // 공개 옷장 리스트 조회
    @Override
    public Page<ClosetGetPublicResponse> getPublicClosets(
            int page,
            int size,
            String sort,
            String direction
    ) {
        log.info("Fetching public closets - page: {}, size: {}, sort: {}, direction: {}", page, size, sort, direction);

        Sort sortObj = Sort.by(Sort.Direction.fromString(direction), sort);

        Pageable pageable = PageRequest.of(
                page,
                size,
                sortObj
        );

        Page<Closet> closets = closetRepository.findAllByIsPublicTrue(pageable);

        log.info("Retrieved {} public closets, totalElements: {}", closets.getContent().size(), closets.getTotalElements());

        return closets.map(ClosetGetPublicResponse::from);
    }

    // 옷장 상세 조회
    @Override
    public ClosetGetResponse getCloset(Long closetId) {
        log.info("Fetching closet details - closetId: {}", closetId);

        Closet closet = closetRepository.findById(closetId)
                .orElseThrow(() -> {
                    log.warn("Closet not found - closetId: {}", closetId);
                    return new ClosetException(ClosetErrorCode.CLOSET_NOT_FOUND);
                });

        if (closet.isDeleted()) {
            log.warn("Closet is deleted - closetId: {}", closetId);
            throw new ClosetException(ClosetErrorCode.CLOSET_DELETED);
        }

        log.debug("Closet retrieved - closetId: {}, name: {}, isPublic: {}",
                closetId, closet.getName(), closet.getIsPublic());

        return ClosetGetResponse.from(closet);
    }

    // 내 옷장 리스트 조회
    @Override
    public Page<ClosetGetMyResponse> getMyClosets(
            Long userId,
            int page,
            int size,
            String sort,
            String direction
    ) {
        log.info("Fetching user's closets - userId: {}, page: {}, size: {}, sort: {}, direction: {}",
                userId, page, size, sort, direction);

        Sort sortObj = Sort.by(Sort.Direction.fromString(direction), sort);

        Pageable pageable = PageRequest.of(
                page,
                size,
                sortObj
        );

        Page<Closet> closets = closetRepository.findAllByUser_Id(
                userId,
                pageable
        );

        log.info("Retrieved {} closets for userId: {}, totalElements: {}",
                closets.getContent().size(), userId, closets.getTotalElements());

        return closets.map(ClosetGetMyResponse::from);
    }

    // 지정된 ID에 해당하는 옷장을 조회
    @Override
    public Closet findClosetById(Long closetId) {
        log.debug("Finding closet by id: {}", closetId);

        return closetRepository.findById(closetId)
                .orElseThrow(() -> {
                    log.warn("Closet not found - closetId: {}", closetId);
                    return new ClosetException(ClosetErrorCode.CLOSET_NOT_FOUND);
                });
    }
}