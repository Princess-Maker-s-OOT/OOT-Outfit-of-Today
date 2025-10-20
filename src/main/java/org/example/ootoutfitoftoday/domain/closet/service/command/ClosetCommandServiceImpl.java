package org.example.ootoutfitoftoday.domain.closet.service.command;

import lombok.RequiredArgsConstructor;
import org.example.ootoutfitoftoday.domain.closet.dto.request.ClosetSaveRequest;
import org.example.ootoutfitoftoday.domain.closet.dto.response.ClosetSaveResponse;
import org.example.ootoutfitoftoday.domain.closet.entity.Closet;
import org.example.ootoutfitoftoday.domain.closet.repository.ClosetRepository;
import org.example.ootoutfitoftoday.domain.user.entity.User;
import org.example.ootoutfitoftoday.domain.user.exception.UserErrorCode;
import org.example.ootoutfitoftoday.domain.user.exception.UserException;
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
    public ClosetSaveResponse createCloset(Long id, ClosetSaveRequest request) {

        User user = userQueryService.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));

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
}