package org.example.ootoutfitoftoday.domain.closet.service.command;

import lombok.RequiredArgsConstructor;
import org.example.ootoutfitoftoday.domain.closet.dto.request.ClosetSaveRequest;
import org.example.ootoutfitoftoday.domain.closet.dto.request.ClosetUpdateRequest;
import org.example.ootoutfitoftoday.domain.closet.dto.response.ClosetSaveResponse;
import org.example.ootoutfitoftoday.domain.closet.dto.response.ClosetUpdateResponse;
import org.example.ootoutfitoftoday.domain.closet.entity.Closet;
import org.example.ootoutfitoftoday.domain.closet.exception.ClosetErrorCode;
import org.example.ootoutfitoftoday.domain.closet.exception.ClosetException;
import org.example.ootoutfitoftoday.domain.closet.repository.ClosetRepository;
import org.example.ootoutfitoftoday.domain.user.entity.User;
import org.example.ootoutfitoftoday.domain.user.service.query.UserQueryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ClosetCommandServiceImpl implements ClosetCommandService {

    private final ClosetRepository closetRepository;
    private final UserQueryService userQueryService;

    // 옷장 등록
    @Override
    public ClosetSaveResponse createCloset(Long id, ClosetSaveRequest request) {

        User user = userQueryService.findByIdAndIsDeletedFalse(id);

        Closet closet = Closet.create(
                user,
                request.name(),
                request.description(),

                request.imageUrl(),
                request.isPublic()
        );

        Closet savedCloset = closetRepository.save(closet);

        return ClosetSaveResponse.from(savedCloset);
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

        if (updatedCloset.isDeleted()) {
            throw new ClosetException(ClosetErrorCode.CLOSET_NOT_FOUND);
        }

        if (!updatedCloset.getUserId().equals(userId)) {
            throw new ClosetException(ClosetErrorCode.CLOSET_FORBIDDEN);
        }

        updatedCloset.update(
                request.name(),
                request.description(),
                request.imageUrl(),
                request.isPublic()
        );

        return ClosetUpdateResponse.from(updatedCloset);
    }
}