package org.example.ootoutfitoftoday.common.response;

import lombok.Builder;
import org.example.ootoutfitoftoday.common.exception.ErrorCode;
import org.example.ootoutfitoftoday.common.exception.SuccessCode;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;

@Builder
public record ApiResponse<T>(
        HttpStatus httpStatus,
        int statusValue,
        boolean success,
        String code,
        String message,
        T data,
        LocalDateTime timestamp
) {

    /**
     * 성공적인 요청에 대한 응답을 반환하는 메서드
     * 주어진 데이터를 포함하여 HTTP 상태 코드와 함께 응답을 반환
     *
     * @param data 요청 성공 시 반환할 데이터
     * @return HTTP 상태코드 성공 응답과 함께 성공 데이터가 포함된 ApiResponseDto
     */
    public static <T> ResponseEntity<ApiResponse<T>> success(T data, SuccessCode successCode) {

        return ResponseEntity.status(successCode.getHttpStatus()).body(
                ApiResponse.<T>builder()
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

    /**
     * 실패한 요청에 대한 응답을 반환하는 메서드
     * 실패에 대한 메시지와 HTTP 에러 응답 코드를 반환
     *
     * @return HTTP 에러 응답 코드와 함께 메시지가 포함된 ApiResponseDto
     */
    // 빌더를 통해서 생성하지 않은 필드는 null 값이 들어가는지 궁금
    public static <T> ApiResponse<T> error(T data, ErrorCode errorCode) {

        return
                ApiResponse.<T>builder()
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
