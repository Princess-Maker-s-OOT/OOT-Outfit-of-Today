package org.example.ootoutfitoftoday.domain.wearrecord.service.command;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.ootoutfitoftoday.domain.clothes.entity.Clothes;
import org.example.ootoutfitoftoday.domain.clothes.service.command.ClothesCommandService;
import org.example.ootoutfitoftoday.domain.clothes.service.query.ClothesQueryService;
import org.example.ootoutfitoftoday.domain.user.entity.User;
import org.example.ootoutfitoftoday.domain.user.service.query.UserQueryService;
import org.example.ootoutfitoftoday.domain.wearrecord.dto.request.WearRecordCreateRequest;
import org.example.ootoutfitoftoday.domain.wearrecord.dto.response.WearRecordCreateResponse;
import org.example.ootoutfitoftoday.domain.wearrecord.entity.WearRecord;
import org.example.ootoutfitoftoday.domain.wearrecord.exception.WearRecordErrorCode;
import org.example.ootoutfitoftoday.domain.wearrecord.exception.WearRecordException;
import org.example.ootoutfitoftoday.domain.wearrecord.repository.WearRecordRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class WearRecordCommandServiceImpl implements WearRecordCommandService {

    private final WearRecordRepository wearRecordRepository;
    private final ClothesCommandService clothesCommandService;
    private final UserQueryService userQueryService;
    private final ClothesQueryService clothesQueryService;

    @Override
    public WearRecordCreateResponse createWearRecord(Long userId, WearRecordCreateRequest request) {
        log.debug("착용 기록 생성 처리 시작 - 사용자 ID: {}, 옷 ID: {}", userId, request.clothesId());

        User user = userQueryService.findByIdAndIsDeletedFalse(userId);
        log.debug("사용자 조회 완료 - 사용자 ID: {}", user.getId());

        Clothes clothes = clothesQueryService.findClothesById(request.clothesId());
        log.debug("옷 조회 완료 - 옷 ID: {}, 옷 설명: {}", clothes.getId(), clothes.getDescription());

        if (!clothes.getUser().getId().equals(userId)) {
            log.warn("착용 기록 생성 권한 없음 - 사용자 ID: {}, 옷 소유자 ID: {}", userId, clothes.getUser().getId());
            throw new WearRecordException(WearRecordErrorCode.WEAR_RECORD_FORBIDDEN);
        }

        LocalDateTime wornAt = LocalDateTime.now();
        WearRecord wearRecord = WearRecord.create(user, clothes, wornAt);
        WearRecord savedRecord = wearRecordRepository.save(wearRecord);
        log.info("착용 기록 저장 완료 - 착용 기록 ID: {}, 착용 시간: {}", savedRecord.getId(), wornAt);

        clothesCommandService.updateLastWornAt(
                request.clothesId(),
                wornAt
        );
        log.debug("옷 마지막 착용 일시 업데이트 완료 - 옷 ID: {}", request.clothesId());

        return WearRecordCreateResponse.from(savedRecord.getId());
    }
}