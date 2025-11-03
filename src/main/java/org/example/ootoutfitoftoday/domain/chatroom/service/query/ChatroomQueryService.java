package org.example.ootoutfitoftoday.domain.chatroom.service.query;

import org.example.ootoutfitoftoday.domain.chatroom.dto.response.ChatroomResponse;
import org.example.ootoutfitoftoday.domain.chatroom.entity.Chatroom;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import java.util.Optional;

public interface ChatroomQueryService {

    Slice<ChatroomResponse> getChatrooms(Long userId, Pageable pageable);

    Chatroom getChatroomById(Long chatroomId);

    /**
     * 사용자와 판매글로 채팅방 조회
     * (거래 시작 시 사용)
     * @return Optional<Chatroom> - 없으면 Empty
     */
    Optional<Chatroom> findByUserAndSalePost(Long userId, Long salePostId);
}
