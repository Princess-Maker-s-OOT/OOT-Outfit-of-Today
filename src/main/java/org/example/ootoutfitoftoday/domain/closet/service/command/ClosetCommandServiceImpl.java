package org.example.ootoutfitoftoday.domain.closet.service.command;

import lombok.RequiredArgsConstructor;
import org.example.ootoutfitoftoday.domain.closet.dto.request.ClosetSaveRequest;
import org.example.ootoutfitoftoday.domain.closet.dto.response.ClosetSaveResponse;
import org.example.ootoutfitoftoday.domain.closet.entity.Closet;
import org.example.ootoutfitoftoday.domain.closet.repository.ClosetRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ClosetCommandServiceImpl implements ClosetCommandService {

    private final ClosetRepository closetRepository;

    // 옷장 등록
    @Override
    public ClosetSaveResponse createCloset(ClosetSaveRequest request) {

        Closet closet = Closet.create(
                request.userId(),
                request.name(),
                request.description(),
                request.imageUrl(),
                request.isPublic()
        );

        Closet savedCloset = closetRepository.save(closet);

        return ClosetSaveResponse.from(savedCloset);
    }
}