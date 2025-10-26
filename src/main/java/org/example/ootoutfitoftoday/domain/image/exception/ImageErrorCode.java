package org.example.ootoutfitoftoday.domain.image.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.example.ootoutfitoftoday.common.exception.ErrorCode;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ImageErrorCode implements ErrorCode {

    INVALID_FILE_NAME("INVALID_FILE_NAME", HttpStatus.BAD_REQUEST, "유효하지 않은 파일명입니다."),
    INVALID_FILE_EXTENSION("INVALID_FILE_EXTENSION", HttpStatus.BAD_REQUEST, "지원하지 않는 파일 형식입니다."),
    INVALID_IMAGE_TYPE("INVALID_IMAGE_TYPE", HttpStatus.BAD_REQUEST, "유효하지 않은 이미지 타입입니다."),
    FILE_SIZE_EXCEEDED("FILE_SIZE_EXCEEDED", HttpStatus.BAD_REQUEST, "파일 크기가 제한을 초과했습니다."),
    PRESIGNED_URL_GENERATION_FAILED("PRESIGNED_URL_GENERATION_FAILED", HttpStatus.INTERNAL_SERVER_ERROR, "Presigned URL 생성에 실패했습니다.");

    private final String code;
    private final HttpStatus httpStatus;
    private final String message;
}
