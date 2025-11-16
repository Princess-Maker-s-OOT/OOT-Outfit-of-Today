package org.example.ootoutfitoftoday.domain.closet.service.command;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.ootoutfitoftoday.domain.closet.dto.request.ClosetCreateRequest;
import org.example.ootoutfitoftoday.domain.closet.dto.request.ClosetUpdateRequest;
import org.example.ootoutfitoftoday.domain.closet.dto.response.ClosetCreateResponse;
import org.example.ootoutfitoftoday.domain.closet.dto.response.ClosetDeleteResponse;
import org.example.ootoutfitoftoday.domain.closet.dto.response.ClosetUpdateResponse;
import org.example.ootoutfitoftoday.domain.closet.entity.Closet;
import org.example.ootoutfitoftoday.domain.closet.exception.ClosetErrorCode;
import org.example.ootoutfitoftoday.domain.closet.exception.ClosetException;
import org.example.ootoutfitoftoday.domain.closet.repository.ClosetRepository;
import org.example.ootoutfitoftoday.domain.image.entity.Image;
import org.example.ootoutfitoftoday.domain.image.service.query.ImageQueryService;
import org.example.ootoutfitoftoday.domain.user.entity.User;
import org.example.ootoutfitoftoday.domain.user.service.query.UserQueryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ClosetCommandServiceImpl implements ClosetCommandService {

    private final ClosetRepository closetRepository;
    private final UserQueryService userQueryService;
    private final ImageQueryService imageQueryService;

    @Override
    public ClosetCreateResponse createCloset(Long userId, ClosetCreateRequest request) {
        log.info("옷장 생성 시작 - 사용자: {}", userId);
        log.debug("옷장 상세 정보 - 이름: {}, 공개여부: {}, 이미지ID: {}",
                request.name(), request.isPublic(), request.imageId());

        User user = userQueryService.findByIdAndIsDeletedFalse(userId);
        log.debug("사용자 조회 완료 - 사용자: {}", user.getId());

        Closet closet = Closet.create(
                user,
                request.name(),
                request.description(),
                request.isPublic()
        );

        if (request.imageId() != null) {
            log.debug("옷장 이미지 연결 - 이미지ID: {}", request.imageId());
            Image image = imageQueryService.findImageById(request.imageId());
            closet.setClosetImage(image);
        }

        Closet savedCloset = closetRepository.save(closet);
        log.info("옷장 생성 완료 - 옷장ID: {}, 사용자: {}", savedCloset.getId(), userId);

        return ClosetCreateResponse.from(savedCloset);
    }

    // 옷장 정보 수정
    @Override
    public ClosetUpdateResponse updateCloset(
            Long userId,
            Long closetId,
            ClosetUpdateRequest request
    ) {
        log.info("옷장 수정 시작 - 옷장ID: {}, 사용자: {}", closetId, userId);
        log.debug("수정 요청 상세 - 이름: {}, 공개여부: {}, 이미지ID: {}",
                request.name(), request.isPublic(), request.imageId());

        Closet updatedCloset = closetRepository.findById(closetId)
                .orElseThrow(() -> {
                    log.warn("옷장을 찾을 수 없음 - 옷장ID: {}", closetId);
                    return new ClosetException(ClosetErrorCode.CLOSET_NOT_FOUND);
                });

        if (!updatedCloset.getUserId().equals(userId)) {
            log.warn("옷장 접근 권한 없음 - 옷장ID: {}, 요청사용자: {}, 소유자: {}",
                    closetId, userId, updatedCloset.getUserId());
            throw new ClosetException(ClosetErrorCode.CLOSET_FORBIDDEN);
        }

        Image newImage = null;
        if (request.imageId() != null) {
            log.debug("옷장 이미지 수정 - 이미지ID: {}", request.imageId());
            newImage = imageQueryService.findImageById(request.imageId());
        }

        updatedCloset.update(
                request.name(),
                request.description(),
                request.isPublic()
        );

        updatedCloset.setClosetImage(newImage);
        log.info("옷장 수정 완료 - 옷장ID: {}", closetId);

        return ClosetUpdateResponse.from(updatedCloset);
    }

    // 옷장 삭제
    @Override
    public ClosetDeleteResponse deleteCloset(
            Long userId,
            Long closetId
    ) {
        log.info("옷장 삭제 시작 - 옷장ID: {}, 사용자: {}", closetId, userId);

        Closet closet = closetRepository.findById(closetId)
                .orElseThrow(() -> {
                    log.warn("삭제할 옷장을 찾을 수 없음 - 옷장ID: {}", closetId);
                    return new ClosetException(ClosetErrorCode.CLOSET_NOT_FOUND);
                });

        if (!Objects.equals(closet.getUserId(), userId)) {
            log.warn("옷장 삭제 권한 없음 - 옷장ID: {}, 요청사용자: {}, 소유자: {}",
                    closetId, userId, closet.getUserId());
            throw new ClosetException(ClosetErrorCode.CLOSET_FORBIDDEN);
        }

        closet.softDelete();
        log.debug("옷장 소프트 삭제 완료 - 옷장ID: {}", closetId);

        if (closet.getClosetImage() != null) {
            closet.getClosetImage().softDelete();
            log.debug("옷장 이미지 소프트 삭제 완료 - 옷장ID: {}", closetId);
        }

        log.info("옷장 삭제 완료 - 옷장ID: {}, 삭제시간: {}", closetId, closet.getDeletedAt());

        return ClosetDeleteResponse.of(closet.getId(), closet.getDeletedAt());
    }
}