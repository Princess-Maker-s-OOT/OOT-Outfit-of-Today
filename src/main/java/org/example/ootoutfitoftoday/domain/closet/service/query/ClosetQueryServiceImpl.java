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

    @Override
    public Page<ClosetGetPublicResponse> getPublicClosets(
            int page,
            int size,
            String sort,
            String direction
    ) {
        log.debug("공개 옷장 목록 조회 시작 - 페이지: {}, 크기: {}, 정렬: {}, 방향: {}", page, size, sort, direction);

        Sort sortObj = Sort.by(Sort.Direction.fromString(direction), sort);

        Pageable pageable = PageRequest.of(
                page,
                size,
                sortObj
        );

        Page<Closet> closets = closetRepository.findAllByIsPublicTrue(pageable);

        log.info("공개 옷장 목록 조회 완료 - 조회 건수: {}, 전체 건수: {}", closets.getContent().size(), closets.getTotalElements());

        return closets.map(ClosetGetPublicResponse::from);
    }

    @Override
    public ClosetGetResponse getCloset(Long closetId) {
        log.info("옷장 상세 조회 시작 - 옷장ID: {}", closetId);

        Closet closet = closetRepository.findById(closetId)
                .orElseThrow(() -> {
                    log.warn("옷장을 찾을 수 없음 - 옷장ID: {}", closetId);
                    return new ClosetException(ClosetErrorCode.CLOSET_NOT_FOUND);
                });

        if (closet.isDeleted()) {
            log.warn("삭제된 옷장 - 옷장ID: {}", closetId);
            throw new ClosetException(ClosetErrorCode.CLOSET_DELETED);
        }

        log.debug("옷장 조회 완료 - 옷장ID: {}, 이름: {}, 공개여부: {}",
                closetId, closet.getName(), closet.getIsPublic());

        return ClosetGetResponse.from(closet);
    }

    @Override
    public Page<ClosetGetMyResponse> getMyClosets(
            Long userId,
            int page,
            int size,
            String sort,
            String direction
    ) {
        log.info("내 옷장 목록 조회 시작 - 사용자: {}, 페이지: {}, 크기: {}, 정렬: {}, 방향: {}",
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

        log.info("내 옷장 목록 조회 완료 - 조회 건수: {}, 사용자: {}, 전체 건수: {}",
                closets.getContent().size(), userId, closets.getTotalElements());

        return closets.map(ClosetGetMyResponse::from);
    }

    @Override
    public Closet findClosetById(Long closetId) {
        log.debug("옷장 조회 - 옷장ID: {}", closetId);

        return closetRepository.findById(closetId)
                .orElseThrow(() -> {
                    log.warn("옷장을 찾을 수 없음 - 옷장ID: {}", closetId);
                    return new ClosetException(ClosetErrorCode.CLOSET_NOT_FOUND);
                });
    }
}