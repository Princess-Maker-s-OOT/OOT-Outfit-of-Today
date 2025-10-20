package org.example.ootoutfitoftoday.domain.chatroom.controller;

import lombok.RequiredArgsConstructor;
import org.example.ootoutfitoftoday.common.response.ApiResponse;
import org.example.ootoutfitoftoday.domain.auth.dto.AuthUser;
import org.example.ootoutfitoftoday.domain.chatroom.dto.request.ChatroomRequest;
import org.example.ootoutfitoftoday.domain.chatroom.exception.ChatroomSuccessCode;
import org.example.ootoutfitoftoday.domain.chatroom.service.command.ChatroomCommandServiceImpl;
import org.example.ootoutfitoftoday.domain.chatroom.service.query.ChatroomQueryServiceImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/chatrooms")
@RequiredArgsConstructor
public class ChatroomController {

    private final ChatroomCommandServiceImpl chatroomCommandServiceImpl;
    private final ChatroomQueryServiceImpl chatroomQueryServiceImpl;

    /**
     * 채팅방 생성 API
     *
     * @param ChatroomRequest 게시물의 id 정보
     * @param authUser        토큰 정보
     * @return 값 x 공통 응답만
     */
    @PostMapping
    public ResponseEntity<ApiResponse<Void>> createChatroom(
            @RequestBody ChatroomRequest ChatroomRequest,
            @AuthenticationPrincipal AuthUser authUser
    ) {
        Long userId = authUser.getUserId();

        chatroomCommandServiceImpl.createChatroom(ChatroomRequest, userId);

        return ApiResponse.success(null, ChatroomSuccessCode.CREATED_CHATROOM);
    }
}
