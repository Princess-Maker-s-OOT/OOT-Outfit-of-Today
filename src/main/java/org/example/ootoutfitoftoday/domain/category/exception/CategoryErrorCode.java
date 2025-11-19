package org.example.ootoutfitoftoday.domain.category.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.example.ootoutfitoftoday.common.exception.ErrorCode;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CategoryErrorCode implements ErrorCode {

    CATEGORY_NOT_FOUND("CATEGORY_NOT_FOUND", HttpStatus.NOT_FOUND, "카테고리를 찾을 수 없습니다."),
    CANNOT_SET_SELF_AS_PARENT("CANNOT_SET_SELF_AS_PARENT", HttpStatus.BAD_REQUEST, "상위 카테고리를 자기 자신으로 설정할 수 없습니다."),
    CATEGORY_CIRCULAR_REFERENCE("CATEGORY_CIRCULAR_REFERENCE", HttpStatus.BAD_REQUEST, "하위 카테고리를 자신의 상위 카테고리로 설정할 수 없습니다.");

    private final String code;
    private final HttpStatus httpStatus;
    private final String message;
}
