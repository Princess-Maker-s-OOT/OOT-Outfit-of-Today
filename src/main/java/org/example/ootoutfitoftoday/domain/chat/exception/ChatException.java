package org.example.ootoutfitoftoday.domain.chat.exception;

import org.example.ootoutfitoftoday.common.exception.GlobalException;

public class ChatException extends GlobalException {
    public ChatException(ChatErrorCode chatErrorCode) {
        super(chatErrorCode);
    }

    public ChatException(ChatErrorCode chatErrorCode, ChatSuccessCode chatSuccessCode) {
        super(chatErrorCode, chatSuccessCode);
    }
}
