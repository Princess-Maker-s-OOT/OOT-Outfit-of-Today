package org.example.ootoutfitoftoday.domain.closet.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.example.ootoutfitoftoday.common.exception.SuccessCode;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ClosetSuccessCode implements SuccessCode {

    CLOSET_CREATED("CLOSET_CREATED", HttpStatus.CREATED, "옷장이 등록되었습니다."),
    CLOSETS_GET_PUBLIC_OK("CLOSETS_GET_PUBLIC_OK", HttpStatus.OK, "공개 옷장 리스트를 조회했습니다."),
    CLOSET_GET_OK("CLOSET_GET_OK", HttpStatus.OK, "옷장 상세 정보를 조회했습니다."),
    CLOSETS_GET_MY_OK("CLOSETS_GET_MY_OK", HttpStatus.OK, "내 옷장 리스트를 조회했습니다."),
    CLOSET_UPDATE_OK("CLOSET_UPDATE_OK", HttpStatus.OK, "옷장 정보가 수정되었습니다.");

    private final String code;
    private final HttpStatus httpStatus;
    private final String message;
}