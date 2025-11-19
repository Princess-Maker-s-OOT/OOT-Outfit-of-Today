package org.example.ootoutfitoftoday.domain.chatparticipatinguser.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.example.ootoutfitoftoday.common.exception.ErrorCode;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ChatParticipatingUserErrorCode implements ErrorCode {

    NOT_MATCH_CHATROOM_AND_USER("NOT_MATCH_CHATROOM_AND_USER", HttpStatus.BAD_REQUEST, "채팅방과 매치되는 유저가 없습니다.");

    private final String code;
    private final HttpStatus httpStatus;
    private final String message;
}
