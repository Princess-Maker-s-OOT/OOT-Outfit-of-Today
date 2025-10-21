package org.example.ootoutfitoftoday.domain.category.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.example.ootoutfitoftoday.common.exception.SuccessCode;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CategorySuccessCode implements SuccessCode {

    CATEGORY_CREATED("CATEGORY_CREATED", HttpStatus.CREATED, "카테고리를 생성 하였습니다!"),
    CATEGORY_OK("CATEGORY_OK", HttpStatus.OK, "카테고리를 조회 하였습니다!"),
    CATEGORY_UPDATE("CATEGORY_UPDATE", HttpStatus.OK, "카테고리를 수정하였습니다!"),
    CATEGORY_DELETE("CATEGORY_DELETE", HttpStatus.OK, "카테고리를 삭제하였습니다!");

    private final String code;
    private final HttpStatus httpStatus;
    private final String message;
}