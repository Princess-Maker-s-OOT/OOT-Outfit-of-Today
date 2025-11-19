package org.example.ootoutfitoftoday.domain.wearrecord.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.example.ootoutfitoftoday.common.exception.ErrorCode;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum WearRecordErrorCode implements ErrorCode {

    WEAR_RECORD_FORBIDDEN("WEAR_RECORD_FORBIDDEN", HttpStatus.FORBIDDEN, "착용 기록을 시도하려는 옷에 대한 권한이 없습니다.");

    private final String code;
    private final HttpStatus httpStatus;
    private final String message;
}