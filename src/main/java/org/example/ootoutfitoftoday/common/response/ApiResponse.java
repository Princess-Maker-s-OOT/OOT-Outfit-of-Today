package org.example.ootoutfitoftoday.common.response;

import lombok.Builder;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;

@Builder
public record ApiResponse<T>(
        HttpStatus httpStatus,
        int statusValue,
        boolean success,
        String message,
        T data,
        LocalDateTime timestamp
) {

    /**
     * 성공적인 요청에 대한 응답을 반환하는 메서드
     * 주어진 데이터를 포함하여 HTTP 200 OK 상태 코드와 함께 응답을 반환
     *
     * @param data 요청 성공 시 반환할 데이터
     * @return HTTP 200 OK 응답과 함께 성공 데이터가 포함된 ApiResponseDto
     */
    public static <T> ResponseEntity<ApiResponse<T>> success(T data, String message) {

        return ResponseEntity.ok(
                ApiResponse.<T>builder()
                        .httpStatus(HttpStatus.OK)
                        .statusValue(HttpStatus.OK.value())
                        .success(true)
                        .message(message)
                        .data(data)
                        .timestamp(LocalDateTime.now())
                        .build()
        );
    }

    /**
     * 성공적인 요청에 대한 응답을 반환하는 메서드
     * 주어진 데이터를 포함하여 HTTP 201 CREATED 상태 코드와 함께 응답을 반환
     *
     * @param data 요청 성공 시 반환할 데이터
     * @return HTTP 201 CREATED 응답과 함께 성공 데이터가 포함된 ApiResponseDto
     */
    public static <T> ResponseEntity<ApiResponse<T>> created(T data, String message) {

        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.<T>builder()
                        .httpStatus(HttpStatus.CREATED)
                        .statusValue(HttpStatus.CREATED.value())
                        .success(true)
                        .message(message)
                        .data(data)
                        .timestamp(LocalDateTime.now())
                        .build()
        );
    }

    /**
     * 성공적인 요청에 대한 응답을 반환하는 메서드
     * 주어진 데이터를 포함하여 HTTP 204 No Content 상태 코드와 함께 응답을 반환
     *
     * @return HTTP 204 No Content 응답과 함께 성공 데이터가 포함된 ApiResponseDto
     */
    public static <T> ResponseEntity<ApiResponse<T>> noContent(String message) {

        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(
                ApiResponse.<T>builder()
                        .httpStatus(HttpStatus.NO_CONTENT)
                        .statusValue(HttpStatus.NO_CONTENT.value())
                        .success(true)
                        .message(message)
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
    public static <T> ApiResponse<T> error(String message, HttpStatus httpStatus) {

        return
                ApiResponse.<T>builder()
                        .httpStatus(httpStatus)
                        .statusValue(httpStatus.value())
                        .success(false)
                        .message(message)
                        .timestamp(LocalDateTime.now())
                        .build();
    }
}
