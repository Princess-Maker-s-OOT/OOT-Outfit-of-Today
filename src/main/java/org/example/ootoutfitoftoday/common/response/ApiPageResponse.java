package org.example.ootoutfitoftoday.common.response;

import lombok.Builder;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record ApiPageResponse<T>(
        HttpStatus httpStatus,
        int statusValue,
        boolean success,
        String message,
        PageData<T> data,
        LocalDateTime timestamp
) {

    /**
     * 성공적인 요청에 대한 페이징 응답을 반환하는 메서드
     * 주어진 데이터를 포함하여 HTTP 200 OK 상태 코드와 함께 응답을 반환
     *
     * @param pagedData 요청 성공 시 반환할 페이징 데이터
     * @return HTTP 200 OK 응답과 함께 ApiPageResponse<T>
     */
    public static <T> ResponseEntity<ApiPageResponse<T>> success(Page<T> pagedData, String message) {

        return ResponseEntity.ok(
                ApiPageResponse.<T>builder()
                        .httpStatus(HttpStatus.OK)
                        .statusValue(HttpStatus.OK.value())
                        .success(true)
                        .message(message)
                        .data(PageData.<T>builder()
                                .content(pagedData.getContent())
                                .totalElements(pagedData.getTotalElements())
                                .totalPages(pagedData.getTotalPages())
                                .size(pagedData.getSize())
                                .number(pagedData.getNumber())
                                .build())
                        .timestamp(java.time.LocalDateTime.now())
                        .build()
        );
    }

    @Builder
    private record PageData<T>(
            List<T> content,
            long totalElements,
            int totalPages,
            int size,
            int number
    ) {
    }
}
