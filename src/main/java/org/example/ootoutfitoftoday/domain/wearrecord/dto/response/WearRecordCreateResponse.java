package org.example.ootoutfitoftoday.domain.wearrecord.dto.response;

import lombok.Builder;

@Builder
public record WearRecordCreateResponse(

        Long wearRecordId
) {
    public static WearRecordCreateResponse from(Long wearRecordId) {

        return WearRecordCreateResponse.builder()
                .wearRecordId(wearRecordId)
                .build();
    }
}