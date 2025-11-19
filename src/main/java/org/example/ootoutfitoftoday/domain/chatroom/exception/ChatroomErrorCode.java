package org.example.ootoutfitoftoday.domain.chatroom.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.example.ootoutfitoftoday.common.exception.ErrorCode;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ChatroomErrorCode implements ErrorCode {

    NOT_EXIST_CHATROOM("NOT_EXIST_CHATROOM", HttpStatus.BAD_REQUEST, "존재하지 않는 채팅방입니다."),
    EQUAL_SELLER_BUYER("EQUAL_SELLER_BUYER", HttpStatus.BAD_REQUEST, "판매자와 구매자가 동일합니다.");

    private final String code;
    private final HttpStatus httpStatus;
    private final String message;
}
