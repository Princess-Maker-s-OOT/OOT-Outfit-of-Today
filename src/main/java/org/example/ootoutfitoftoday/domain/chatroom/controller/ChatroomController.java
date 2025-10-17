package org.example.ootoutfitoftoday.domain.chatroom.controller;

import lombok.RequiredArgsConstructor;
import org.example.ootoutfitoftoday.common.response.ApiResponse;
import org.example.ootoutfitoftoday.domain.chatroom.dto.request.ChatroomRequest;
import org.example.ootoutfitoftoday.domain.chatroom.exception.ChatroomSuccessCode;
import org.example.ootoutfitoftoday.domain.chatroom.service.command.ChatroomCommandServiceImpl;
import org.example.ootoutfitoftoday.domain.chatroom.service.query.ChatroomQueryServiceImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/chatrooms")
@RequiredArgsConstructor
public class ChatroomController {

    private final ChatroomCommandServiceImpl chatroomCommandServiceImpl;
    private final ChatroomQueryServiceImpl chatroomQueryServiceImpl;

    /**
     * @param ChatroomRequest 게시물의 id 정보
     * @param authUser        토큰 정보
     * @return 값 x
     */
    @PostMapping
    public ResponseEntity<ApiResponse<Void>> createChatroom(
            @RequestBody ChatroomRequest ChatroomRequest,
            @AuthenticationPrincipal AuthUser authUser
    ) {
        Long userId = authUser.getId();

        chatroomCommandServiceImpl.createChatroom(ChatroomRequest, userId);

        return ApiResponse.created(null, ChatroomSuccessCode.CREATED_CHATROOM.getMessage());
    }
}
