package org.example.ootoutfitoftoday.domain.closet.service.command;

import lombok.RequiredArgsConstructor;
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

        User user = userQueryService.findByIdAndIsDeletedFalse(userId);

        Closet closet = Closet.create(
                user,
                request.name(),
                request.description(),
                request.isPublic()
        );

        if (request.imageId() != null) {
            Image image = imageQueryService.findImageById(request.imageId());
            closet.setClosetImage(image);
        }

        Closet savedCloset = closetRepository.save(closet);

        return ClosetCreateResponse.from(savedCloset);
    }

    // 옷장 정보 수정
    @Override
    public ClosetUpdateResponse updateCloset(
            Long userId,
            Long closetId,
            ClosetUpdateRequest request
    ) {
        Closet updatedCloset = closetRepository.findById(closetId)
                .orElseThrow(() -> new ClosetException(ClosetErrorCode.CLOSET_NOT_FOUND));

        if (!updatedCloset.getUserId().equals(userId)) {
            throw new ClosetException(ClosetErrorCode.CLOSET_FORBIDDEN);
        }

        Image newImage = null;
        if (request.imageId() != null) {
            newImage = imageQueryService.findImageById(request.imageId());
        }

        updatedCloset.update(
                request.name(),
                request.description(),
                request.isPublic()
        );

        updatedCloset.setClosetImage(newImage);

        return ClosetUpdateResponse.from(updatedCloset);
    }

    // 옷장 삭제
    @Override
    public ClosetDeleteResponse deleteCloset(
            Long userId,
            Long closetId
    ) {
        Closet closet = closetRepository.findById(closetId)
                .orElseThrow(() -> new ClosetException(ClosetErrorCode.CLOSET_NOT_FOUND));

        if (!Objects.equals(closet.getUserId(), userId)) {
            throw new ClosetException(ClosetErrorCode.CLOSET_FORBIDDEN);
        }

        closet.softDelete();

        if (closet.getClosetImage() != null) {
            closet.getClosetImage().softDelete();
        }

        return ClosetDeleteResponse.of(closet.getId(), closet.getDeletedAt());
    }
}