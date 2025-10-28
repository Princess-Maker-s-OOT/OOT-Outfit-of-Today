package org.example.ootoutfitoftoday.domain.wearrecord.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.example.ootoutfitoftoday.common.exception.SuccessCode;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum WearRecordSuccessCode implements SuccessCode {

    WEAR_RECORD_CREATED("WEAR_RECORD_CREATED", HttpStatus.CREATED, "착용 기록 등록에 성공했습니다.");

    private final String code;
    private final HttpStatus httpStatus;
    private final String message;
}