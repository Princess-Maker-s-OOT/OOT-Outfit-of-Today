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
