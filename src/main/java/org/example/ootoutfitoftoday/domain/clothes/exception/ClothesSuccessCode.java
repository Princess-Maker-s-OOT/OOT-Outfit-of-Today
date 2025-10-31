package org.example.ootoutfitoftoday.domain.clothes.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.example.ootoutfitoftoday.common.exception.SuccessCode;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ClothesSuccessCode implements SuccessCode {

    CLOTHES_CREATED("CLOTHES_CREATED", HttpStatus.CREATED, "옷을 등록하였습니다!"),
    CLOTHES_OK("CLOTHES_OK",HttpStatus.OK, "옷을 성공적으로 조회하였습니다!"),
    CLOTHES_UPDATE("CLOTHES_UPDATE",HttpStatus.OK, "옷을 성공적으로 수정하였습니다!"),
    CLOTHES_DELETE("CLOTHES_DELETE",HttpStatus.OK, "옷을 성공적으로 삭제하였습니다!"),
    CLOTHES_IMAGE_REMOVE("CLOTHES_IMAGE_REMOVE",HttpStatus.OK, "옷에 등록된 이미지를 성공적으로 제거하였습니다!");

    private final String code;
    private final HttpStatus httpStatus;
    private final String message;
}


