package org.example.ootoutfitoftoday.domain.chat.service.query;

import org.example.ootoutfitoftoday.domain.chat.entity.Chat;
import org.example.ootoutfitoftoday.domain.chatroom.entity.Chatroom;

public interface ChatReferenceToChatroomService {

    Chat getFinalChat(Chatroom chatroom);
}
