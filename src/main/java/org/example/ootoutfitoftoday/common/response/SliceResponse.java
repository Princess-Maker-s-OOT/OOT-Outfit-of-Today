package org.example.ootoutfitoftoday.common.response;

import lombok.Builder;
import org.example.ootoutfitoftoday.common.exception.SuccessCode;
import org.springframework.data.domain.Slice;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record SliceResponse<T>(
        HttpStatus httpStatus,
        int statusValue,
        boolean success,
        String code,
        String message,
        SliceData<T> data,
        LocalDateTime timestamp
) {

    /**
     * 성공적인 요청에 대한 페이징 응답을 반환하는 메서드
     * 주어진 데이터를 포함하여 HTTP 상태 코드와 함께 응답을 반환
     *
     * @param sliceData 요청 성공 시 반환할 페이징 데이터
     * @return HTTP 상태코드 성공 응답과 함께 ApiSliceResponse<T>
     */
    public static <T> ResponseEntity<SliceResponse<T>> success(Slice<T> sliceData, SuccessCode successCode) {

        return ResponseEntity.status(successCode.getHttpStatus()).body(
                SliceResponse.<T>builder()
                        .httpStatus(successCode.getHttpStatus())
                        .statusValue(successCode.getHttpStatus().value())
                        .success(true)
                        .code(successCode.getCode())
                        .message(successCode.getMessage())
                        .data(SliceData.<T>builder()
                                .content(sliceData.getContent())
                                .size(sliceData.getSize())
                                .number(sliceData.getNumber())
                                .hasNext(sliceData.hasNext())
                                .hasPrevious(sliceData.hasPrevious())
                                .build())
                        .timestamp(java.time.LocalDateTime.now())
                        .build()
        );
    }

    @Builder
    private record SliceData<T>(
            List<T> content,
            int size,
            int number,
            boolean hasNext,
            boolean hasPrevious
    ) {
    }
}
