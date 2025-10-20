package org.example.ootoutfitoftoday.domain.chatroom.service.query;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.ootoutfitoftoday.domain.chatparticipatinguser.entity.ChatParticipatingUser;
import org.example.ootoutfitoftoday.domain.chatparticipatinguser.service.query.ChatParticipatingUserQueryService;
import org.example.ootoutfitoftoday.domain.chatroom.dto.response.ChatroomResponse;
import org.example.ootoutfitoftoday.domain.chatroom.repository.ChatroomRepository;
import org.example.ootoutfitoftoday.domain.user.entity.User;
import org.example.ootoutfitoftoday.domain.user.service.query.UserQueryService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ChatroomQueryServiceImpl {

    private final ChatroomRepository chatroomRepository;
    private final ChatParticipatingUserQueryService chatParticipatingUserQueryService;
    private final UserQueryService userQueryService;
//    private final ChatQueryService chatQueryService;

    // 채팅방 조회
    public Slice<ChatroomResponse> getChatrooms(Long userId, Pageable pageable) {
        // 상대방 이름
        // 1. 해당 유저의 id로 User 객체를 찾는다.
        User user = userQueryService.findByIdAndIsDeletedFalse(userId).orElse(null);
        // 2. 중간테이블을 통해 복합키 <Chatroomm, User>를 찾는다.
        List<ChatParticipatingUser> allChatroomsAndUsers = chatParticipatingUserQueryService.getChatParticipatingUsers(user);
        // 3. 현재 유저가 포함 되어있는 복합키는 제외한다.
        List<ChatParticipatingUser> chatroomAndUsers = allChatroomsAndUsers.stream()
                .filter(chatParticipatingUser -> !chatParticipatingUser.getUser().equals(user))
                .toList();
        // 4. 찾은 유저의 정보를 userQueryService.findByIdAndIsDeletedFalse(다른 유저)를 통해 이름을 얻는다.

        // 1. 위 로직을 통해 찾은 chatroom id를 chatQueryService.findByChatroomId...(chatroomId); 마지막 채팅 찾기-> 고려할 점 - 메세지 삭제 여부 등
//        - 마지막 채팅
//        - 현재 시간 - 마지막 채팅 시간 -> 정렬 기준으로 사용하는 필드

        // 2. chatQueryService.findByChatroomId...(chatroomId) 읽음 여부 false인 채팅 count 위와 다른 쿼리 -> 고려할 점 - 메시지 삭제 여부, 읽음 여부 등
//        - 읽지 않은 채팅 개수

    }
}
