package org.example.ootoutfitoftoday.domain.chat.service.query;

import org.example.ootoutfitoftoday.domain.chat.dto.response.ChatResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

public interface ChatQueryService {

    Slice<ChatResponse> getChats(Long chatroomId, Pageable pageable);

    /**
     * 채팅방에 채팅 내역이 존재하는지 확인
     * (거래 시작 전 검증용)
     */
    boolean existsByChatroom(Long chatroomId);
}
