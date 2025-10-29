package org.example.ootoutfitoftoday.domain.wearrecord.dto.response;

public record WearRecordCreateResponse(

        Long wearRecordId
) {
    public static WearRecordCreateResponse from(Long wearRecordId) {

        return new WearRecordCreateResponse(wearRecordId);
    }
}