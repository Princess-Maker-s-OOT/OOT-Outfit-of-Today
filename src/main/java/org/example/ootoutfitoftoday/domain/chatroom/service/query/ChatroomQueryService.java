package org.example.ootoutfitoftoday.domain.chatroom.service.query;

import org.example.ootoutfitoftoday.domain.chatroom.dto.response.ChatroomResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

public interface ChatroomQueryService {

    Slice<ChatroomResponse> getChatrooms(Long userId, Pageable pageable);
}
