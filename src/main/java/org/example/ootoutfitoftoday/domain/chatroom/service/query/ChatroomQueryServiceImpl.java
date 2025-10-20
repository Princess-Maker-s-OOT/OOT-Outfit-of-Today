package org.example.ootoutfitoftoday.domain.chatroom.service.query;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.ootoutfitoftoday.domain.chat.entity.Chat;
import org.example.ootoutfitoftoday.domain.chat.service.query.ChatQueryService;
import org.example.ootoutfitoftoday.domain.chatparticipatinguser.entity.ChatParticipatingUser;
import org.example.ootoutfitoftoday.domain.chatparticipatinguser.service.query.ChatParticipatingUserQueryService;
import org.example.ootoutfitoftoday.domain.chatroom.dto.response.ChatroomResponse;
import org.example.ootoutfitoftoday.domain.user.entity.User;
import org.example.ootoutfitoftoday.domain.user.service.query.UserQueryService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ChatroomQueryServiceImpl implements ChatroomQueryService {

    private final ChatParticipatingUserQueryService chatParticipatingUserQueryService;
    private final UserQueryService userQueryService;
    private final ChatQueryService chatQueryService;

    // 채팅방 조회
    @Override
    public Slice<ChatroomResponse> getChatrooms(Long userId, Pageable pageable) {
        // 상대방 이름
        // 1. 해당 유저의 id로 User 객체를 찾는다.
        User user = userQueryService.findByIdAndIsDeletedFalse(userId);
        // 2. 중간테이블을 통해 복합키 <Chatroomm, User>를 찾는다.
        List<ChatParticipatingUser> getChatroomsAndBuyers = chatParticipatingUserQueryService.getChatParticipatingUsers(user);

        List<ChatParticipatingUser> getChatroomsAndSellers = new ArrayList<>();

        getChatroomsAndBuyers
                .forEach(chatParticipatingUser -> {
                    AtomicBoolean isAllDeleted = new AtomicBoolean(false);
                    List<ChatParticipatingUser> getChatroomsAndUsers = chatParticipatingUserQueryService.getAllParticipatingUserByChatroom(chatParticipatingUser.getChatroom());
                    getChatroomsAndUsers
                            .forEach(chatParticipatingUser1 -> {
                                if (Objects.equals(chatParticipatingUser1.getUser(), user) && chatParticipatingUser1.isDeleted()) {
                                    isAllDeleted.set(true);
                                }
                            });

                    // 3. 현재 유저가 포함 되어있는 복합키는 제외한다.
                    if (!isAllDeleted.get()) {
                        getChatroomsAndUsers
                                .forEach(chatParticipatingUser2 -> {
                                    if (Objects.equals(chatParticipatingUser2.getUser(), user)) {
                                        getChatroomsAndSellers.add(chatParticipatingUser2);
                                    }
                                });
                    }
                });

        List<ChatroomResponse> listChatroomResponse = new ArrayList<>();
        getChatroomsAndBuyers
                .forEach(chatParticipatingUser -> {
                    // 4. 찾은 유저의 정보를 userQueryService.findByIdAndIsDeletedFalse(다른 유저)를 통해 이름을 얻는다.
                    String otherUsername = chatParticipatingUser.getUser().getNickname();
                    log.info("채팅방 상대 유저 이름 : {}", otherUsername);
                    // 5. 위 로직을 통해 찾은 chatroom id를 chatQueryService.findByChatroomId...(chatroomId); 마지막 채팅 찾기
                    Chat chat = chatQueryService.getFinalChat(chatParticipatingUser.getChatroom());
                    String content = (chat != null) ? chat.getContent() : null;
                    Duration time = (chat != null) ? Duration.between(LocalDateTime.now(), chat.getCreatedAt()) : null;
                    // 6. chatQueryService.findByChatroomId...(chatroomId) 읽지 않은 채팅 개수
                    int count = chatQueryService.getCountNotReadChat(chatParticipatingUser.getChatroom());

                    ChatroomResponse chatroomResponse = ChatroomResponse.from(
                            otherUsername,
                            content,
                            time,
                            count
                    );
                    listChatroomResponse.add(chatroomResponse);
                });

        // 최신순으로 정렬
        listChatroomResponse.sort(Comparator.comparing(ChatroomResponse::getAfterFinalChatTime).reversed());

        // List -> Slice로 변환
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), listChatroomResponse.size());
        List<ChatroomResponse> subList = listChatroomResponse.subList(start, end);
        boolean hasNext = end < listChatroomResponse.size();

        return new SliceImpl<>(subList, pageable, hasNext);
    }
}
