package org.example.ootoutfitoftoday.domain.chat.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.example.ootoutfitoftoday.common.exception.SuccessCode;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ChatSuccessCode implements SuccessCode {

    RETRIEVED_CHATS("RETRIEVED_CHATS", HttpStatus.OK, "채팅 조회에 성공하였습니다.");

    private final String code;
    private final HttpStatus httpStatus;
    private final String message;
}
