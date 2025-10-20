package org.example.ootoutfitoftoday.domain.chatroom.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.example.ootoutfitoftoday.common.exception.SuccessCode;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ChatroomSuccessCode implements SuccessCode {

    CREATED_CHATROOM("CREATED_CHATROOM", HttpStatus.CREATED, "채팅방 생성에 성공하였습니다."),
    RETRIEVED_CHATROOMS("RETRIEVED_CHATROOMS", HttpStatus.OK, "채팅방 조회에 성공하였습니다.");

    private final String code;
    private final HttpStatus httpStatus;
    private final String message;
}
