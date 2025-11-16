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

    // 옷장 등록
    @Override
    public ClosetCreateResponse createCloset(Long userId, ClosetCreateRequest request) {
        log.info("Creating closet for userId: {}", userId);
        log.debug("Closet details - name: {}, isPublic: {}, imageId: {}",
                request.name(), request.isPublic(), request.imageId());

        User user = userQueryService.findByIdAndIsDeletedFalse(userId);
        log.debug("User found: {}", user.getId());

        Closet closet = Closet.create(
                user,
                request.name(),
                request.description(),
                request.isPublic()
        );

        if (request.imageId() != null) {
            log.debug("Attaching image to closet - imageId: {}", request.imageId());
            Image image = imageQueryService.findImageById(request.imageId());
            closet.setClosetImage(image);
        }

        Closet savedCloset = closetRepository.save(closet);
        log.info("Closet created successfully - closetId: {}, userId: {}", savedCloset.getId(), userId);

        return ClosetCreateResponse.from(savedCloset);
    }

    // 옷장 정보 수정
    @Override
    public ClosetUpdateResponse updateCloset(
            Long userId,
            Long closetId,
            ClosetUpdateRequest request
    ) {
        log.info("Updating closet - closetId: {}, userId: {}", closetId, userId);
        log.debug("Update request - name: {}, isPublic: {}, imageId: {}",
                request.name(), request.isPublic(), request.imageId());

        Closet updatedCloset = closetRepository.findById(closetId)
                .orElseThrow(() -> {
                    log.warn("Closet not found - closetId: {}", closetId);
                    return new ClosetException(ClosetErrorCode.CLOSET_NOT_FOUND);
                });

        if (!updatedCloset.getUserId().equals(userId)) {
            log.warn("Unauthorized access to closet - closetId: {}, requestUserId: {}, ownerUserId: {}",
                    closetId, userId, updatedCloset.getUserId());
            throw new ClosetException(ClosetErrorCode.CLOSET_FORBIDDEN);
        }

        Image newImage = null;
        if (request.imageId() != null) {
            log.debug("Updating closet image - imageId: {}", request.imageId());
            newImage = imageQueryService.findImageById(request.imageId());
        }

        updatedCloset.update(
                request.name(),
                request.description(),
                request.isPublic()
        );

        updatedCloset.setClosetImage(newImage);
        log.info("Closet updated successfully - closetId: {}", closetId);

        return ClosetUpdateResponse.from(updatedCloset);
    }

    // 옷장 삭제
    @Override
    public ClosetDeleteResponse deleteCloset(
            Long userId,
            Long closetId
    ) {
        log.info("Deleting closet - closetId: {}, userId: {}", closetId, userId);

        Closet closet = closetRepository.findById(closetId)
                .orElseThrow(() -> {
                    log.warn("Closet not found for deletion - closetId: {}", closetId);
                    return new ClosetException(ClosetErrorCode.CLOSET_NOT_FOUND);
                });

        if (!Objects.equals(closet.getUserId(), userId)) {
            log.warn("Unauthorized deletion attempt - closetId: {}, requestUserId: {}, ownerUserId: {}",
                    closetId, userId, closet.getUserId());
            throw new ClosetException(ClosetErrorCode.CLOSET_FORBIDDEN);
        }

        closet.softDelete();
        log.debug("Closet soft deleted - closetId: {}", closetId);

        if (closet.getClosetImage() != null) {
            closet.getClosetImage().softDelete();
            log.debug("Closet image soft deleted - closetId: {}", closetId);
        }

        log.info("Closet deleted successfully - closetId: {}, deletedAt: {}", closetId, closet.getDeletedAt());

        return ClosetDeleteResponse.of(closet.getId(), closet.getDeletedAt());
    }
}