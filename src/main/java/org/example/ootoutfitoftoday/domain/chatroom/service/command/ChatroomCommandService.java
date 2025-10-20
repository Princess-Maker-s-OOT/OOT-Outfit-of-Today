package org.example.ootoutfitoftoday.domain.chatroom.service.command;

import org.example.ootoutfitoftoday.domain.chatroom.dto.request.ChatroomRequest;

public interface ChatroomCommandService {

    void createChatroom(ChatroomRequest chatroomRequest, Long userId);

    void deleteChatroom(Long chatroomId, Long userId);
}
