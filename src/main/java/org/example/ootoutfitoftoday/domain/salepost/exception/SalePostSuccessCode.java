package org.example.ootoutfitoftoday.domain.salepost.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.example.ootoutfitoftoday.common.exception.SuccessCode;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum SalePostSuccessCode implements SuccessCode {

    SALE_POST_CREATED("SALE_POST_CREATED", HttpStatus.CREATED, "판매글이 성공적으로 생성되었습니다."),
    SALE_POST_RETRIEVED("SALE_POST_RETRIEVED", HttpStatus.OK, "판매글이 성공적으로 조회되었습니다."),
    SALE_POST_FETCHED("SALE_POST_FETCHED", HttpStatus.OK, "판매글 목록이 성공적으로 조회되었습니다."),
    SALE_POST_UPDATED("SALE_POST_UPDATED", HttpStatus.OK, "판매글이 수정되었습니다."),
    SALE_POST_DELETED("SALE_POST_DELETED", HttpStatus.OK, "판매글이 삭제되었습니다.");

    private final String code;
    private final HttpStatus httpStatus;
    private final String message;
}
