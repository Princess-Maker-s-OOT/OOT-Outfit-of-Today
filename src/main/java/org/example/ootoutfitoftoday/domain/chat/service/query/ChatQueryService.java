package org.example.ootoutfitoftoday.domain.chat.service.query;

import org.example.ootoutfitoftoday.domain.chat.dto.response.ChatResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

public interface ChatQueryService {

    Slice<ChatResponse> getChats(Long chatroomId, Pageable pageable);
}
