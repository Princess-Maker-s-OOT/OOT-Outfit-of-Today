package org.example.ootoutfitoftoday.common.response;

import lombok.Builder;
import org.example.ootoutfitoftoday.common.exception.ErrorCode;
import org.example.ootoutfitoftoday.common.exception.SuccessCode;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;

@Builder
public record Response<T>(
        HttpStatus httpStatus,
        int statusValue,
        boolean success,
        String code,
        String message,
        T data,
        LocalDateTime timestamp
) {
    public static <T> ResponseEntity<Response<T>> success(T data, SuccessCode successCode) {

        return ResponseEntity.status(successCode.getHttpStatus()).body(
                Response.<T>builder()
                        .httpStatus(successCode.getHttpStatus())
                        .statusValue(successCode.getHttpStatus().value())
                        .success(true)
                        .code(successCode.getCode())
                        .message(successCode.getMessage())
                        .data(data)
                        .timestamp(LocalDateTime.now())
                        .build()
        );
    }

    public static <T> Response<T> error(T data, ErrorCode errorCode) {

        return Response.<T>builder()
                .httpStatus(errorCode.getHttpStatus())
                .statusValue(errorCode.getHttpStatus().value())
                .success(false)
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
