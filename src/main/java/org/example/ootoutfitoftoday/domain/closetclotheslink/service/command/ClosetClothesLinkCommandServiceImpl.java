package org.example.ootoutfitoftoday.domain.closetclotheslink.service.command;

import lombok.RequiredArgsConstructor;
import org.example.ootoutfitoftoday.domain.closet.entity.Closet;
import org.example.ootoutfitoftoday.domain.closet.service.query.ClosetQueryService;
import org.example.ootoutfitoftoday.domain.closetclotheslink.dto.request.ClosetClothesLinkRequest;
import org.example.ootoutfitoftoday.domain.closetclotheslink.dto.response.ClosetClothesLinkDeleteResponse;
import org.example.ootoutfitoftoday.domain.closetclotheslink.dto.response.ClosetClothesLinkResponse;
import org.example.ootoutfitoftoday.domain.closetclotheslink.entity.ClosetClothesLink;
import org.example.ootoutfitoftoday.domain.closetclotheslink.exception.ClosetClothesLinkErrorCode;
import org.example.ootoutfitoftoday.domain.closetclotheslink.exception.ClosetClothesLinkException;
import org.example.ootoutfitoftoday.domain.closetclotheslink.repository.ClosetClothesLinkRepository;
import org.example.ootoutfitoftoday.domain.clothes.entity.Clothes;
import org.example.ootoutfitoftoday.domain.clothes.service.query.ClothesQueryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional
public class ClosetClothesLinkCommandServiceImpl implements ClosetClothesLinkCommandService {

    private final ClosetQueryService closetQueryService;
    private final ClothesQueryService clothesQueryService;
    private final ClosetClothesLinkRepository closetClothesLinkRepository;

    // 특정 옷장에 옷 등록
    @Override
    public ClosetClothesLinkResponse createClosetClothesLink(
            Long userId,
            Long closetId,
            ClosetClothesLinkRequest request
    ) {

        Closet closet = closetQueryService.findClosetById(closetId);

        if (!Objects.equals(closet.getUserId(), userId)) {
            throw new ClosetClothesLinkException(ClosetClothesLinkErrorCode.CLOSET_CLOTHES_FORBIDDEN);
        }

        Clothes clothes = clothesQueryService.findClothesById(request.clothesId());

        // 중복 연결 체크
        if (closetClothesLinkRepository.existsByClosetIdAndClothesId(closetId, request.clothesId())) {
            throw new ClosetClothesLinkException(ClosetClothesLinkErrorCode.CLOSET_CLOTHES_ALREADY_LINKED);
        }

        // 연결 생성
        ClosetClothesLink link = ClosetClothesLink.create(closet, clothes);
        ClosetClothesLink savedLink = closetClothesLinkRepository.save(link);

        return ClosetClothesLinkResponse.from(savedLink);
    }

    // 특정 옷장의 옷 삭제
    @Override
    public ClosetClothesLinkDeleteResponse deleteClosetClothesLink(
            Long userId,
            Long closetId,
            Long clothesId
    ) {

        Closet closet = closetQueryService.findClosetById(closetId);

        if (!Objects.equals(closet.getUserId(), userId)) {
            throw new ClosetClothesLinkException(ClosetClothesLinkErrorCode.CLOSET_CLOTHES_FORBIDDEN);
        }

        // 연결 조회 (삭제되지 않은 것만)
        ClosetClothesLink link = closetClothesLinkRepository
                .findByClosetIdAndClothesIdAndIsDeletedFalse(closetId, clothesId)
                .orElseThrow(() -> new ClosetClothesLinkException(ClosetClothesLinkErrorCode.CLOTHES_NOT_LINKED));

        link.softDelete();

        return ClosetClothesLinkDeleteResponse.of(closetId, clothesId);
    }
}