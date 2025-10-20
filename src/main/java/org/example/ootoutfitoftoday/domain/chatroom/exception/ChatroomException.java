package org.example.ootoutfitoftoday.domain.chatroom.exception;

import org.example.ootoutfitoftoday.common.exception.GlobalException;

public class ChatroomException extends GlobalException {
    public ChatroomException(ChatroomErrorCode chatroomErrorCode) {
        super(chatroomErrorCode);
    }

    public ChatroomException(ChatroomErrorCode chatroomErrorCode, ChatroomSuccessCode chatroomSuccessCode) {
        super(chatroomErrorCode, chatroomSuccessCode);
    }
}
