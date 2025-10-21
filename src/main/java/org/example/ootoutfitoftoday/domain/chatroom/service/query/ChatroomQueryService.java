package org.example.ootoutfitoftoday.domain.chatroom.service.query;

import org.example.ootoutfitoftoday.domain.chatroom.dto.response.ChatroomResponse;
import org.example.ootoutfitoftoday.domain.chatroom.entity.Chatroom;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

public interface ChatroomQueryService {

    Slice<ChatroomResponse> getChatrooms(Long userId, Pageable pageable);

    Chatroom getChatroomById(Long chatroomId);
}
