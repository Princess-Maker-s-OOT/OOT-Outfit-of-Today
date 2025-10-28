package org.example.ootoutfitoftoday.domain.wearrecord.service.command;

import lombok.RequiredArgsConstructor;
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


        User user = userQueryService.findByIdAndIsDeletedFalse(userId);

        Clothes clothes = clothesQueryService.findClothesById(request.clothesId());

        if (!clothes.getUser().getId().equals(userId)) {
            throw new WearRecordException(WearRecordErrorCode.WEAR_RECORD_FORBIDDEN);
        }

        LocalDateTime wornAt = LocalDateTime.now();
        WearRecord wearRecord = WearRecord.create(user, clothes, wornAt);
        WearRecord savedRecord = wearRecordRepository.save(wearRecord);

        // Clothes 마지막 착용 일시 업데이트 위임
        clothesCommandService.updateLastWornAt(
                request.clothesId(),
                wornAt
        );

        return WearRecordCreateResponse.from(savedRecord.getId());
    }
}