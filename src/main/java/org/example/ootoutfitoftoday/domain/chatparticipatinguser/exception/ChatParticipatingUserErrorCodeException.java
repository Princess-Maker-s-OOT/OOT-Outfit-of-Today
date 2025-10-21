package org.example.ootoutfitoftoday.domain.chatparticipatinguser.exception;

import org.example.ootoutfitoftoday.common.exception.GlobalException;

public class ChatParticipatingUserErrorCodeException extends GlobalException {
    public ChatParticipatingUserErrorCodeException(ChatParticipatingUserErrorCode chatParticipatingUserErrorCodeExceptionErrorCode) {
        super(chatParticipatingUserErrorCodeExceptionErrorCode);
    }
}
