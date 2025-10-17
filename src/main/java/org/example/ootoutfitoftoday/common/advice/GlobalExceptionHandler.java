package org.example.ootoutfitoftoday.common.advice;

import lombok.extern.slf4j.Slf4j;
import org.example.ootoutfitoftoday.common.exception.CommonErrorCode;
import org.example.ootoutfitoftoday.common.exception.ErrorCode;
import org.example.ootoutfitoftoday.common.exception.GlobalException;
import org.example.ootoutfitoftoday.common.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception ex) {
        log.error("알 수 없는 서버 오류 발생 ", ex);
        return ResponseEntity
                .status(CommonErrorCode.UNEXPECTED_SERVER_ERROR.getHttpStatus())
                .body(ApiResponse.error(CommonErrorCode.UNEXPECTED_SERVER_ERROR));
    }

    @ExceptionHandler(GlobalException.class)
    public ResponseEntity<ApiResponse<Void>> handleGlobalException(GlobalException ex) {
        log.error("비즈니스 오류 발생 ", ex);
        return handleExceptionInternal(ex.getErrorCode());
    }

    private ResponseEntity<ApiResponse<Void>> handleExceptionInternal(ErrorCode errorCode) {
        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(ApiResponse.error(errorCode));
    }

    // Validation Exception은 BAD_REQUEST로 통일
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        // 첫 번째 에러 메시지 가져오기
        // String errorMessage = ex.getBindingResult().getAllErrors().get(0).getDefaultMessage();

        return ResponseEntity
                .status(CommonErrorCode.VALIDATION_ERROR.getHttpStatus())
                .body(ApiResponse.error(CommonErrorCode.VALIDATION_ERROR));
    }
}
