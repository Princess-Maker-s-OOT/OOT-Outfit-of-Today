package org.example.ootoutfitoftoday.domain.chatroom.service.query;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.ootoutfitoftoday.domain.chat.entity.Chat;
import org.example.ootoutfitoftoday.domain.chat.service.query.ChatReferenceToChatroomQueryService;
import org.example.ootoutfitoftoday.domain.chatparticipatinguser.entity.ChatParticipatingUser;
import org.example.ootoutfitoftoday.domain.chatparticipatinguser.service.query.ChatParticipatingUserQueryService;
import org.example.ootoutfitoftoday.domain.chatroom.dto.response.ChatroomResponse;
import org.example.ootoutfitoftoday.domain.chatroom.entity.Chatroom;
import org.example.ootoutfitoftoday.domain.chatroom.exception.ChatroomErrorCode;
import org.example.ootoutfitoftoday.domain.chatroom.exception.ChatroomException;
import org.example.ootoutfitoftoday.domain.chatroom.repository.ChatroomRepository;
import org.example.ootoutfitoftoday.domain.user.entity.User;
import org.example.ootoutfitoftoday.domain.user.service.query.UserQueryService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ChatroomQueryServiceImpl implements ChatroomQueryService {

    private final ChatroomRepository chatroomRepository;
    private final ChatParticipatingUserQueryService chatParticipatingUserQueryService;
    private final UserQueryService userQueryService;
    private final ChatReferenceToChatroomQueryService chatReferenceToChatroomQueryService;

    // 채팅방 조회
    @Override
    public Slice<ChatroomResponse> getChatrooms(Long userId, Pageable pageable) {
        User currentUser = userQueryService.findByIdAndIsDeletedFalse(userId);

        // 1. 사용자가 참여하고 있는 채팅방 목록을 가져옵니다.
        List<ChatParticipatingUser> userParticipations = chatParticipatingUserQueryService.getChatParticipatingUsers(currentUser);

        List<ChatroomResponse> chatroomResponses = userParticipations.stream()
                .map(participation -> {
                    Chatroom chatroom = participation.getChatroom();

                    // 2. 채팅방의 다른 참여자 정보를 가져옵니다. (N+1 문제 개선 필요)
                    String otherUserNickname = chatParticipatingUserQueryService.getAllParticipatingUserByChatroom(chatroom)
                            .stream()
                            .map(ChatParticipatingUser::getUser)
                            .filter(u -> !u.getId().equals(userId))
                            .findFirst()
                            .map(User::getNickname)
                            .orElse("알 수 없는 사용자");

                    // 3. 마지막 채팅과 읽지 않은 채팅 수를 가져옵니다. (N+1 문제 개선 필요)
                    Chat finalChat = chatReferenceToChatroomQueryService.getFinalChat(chatroom);

                    String finalChatContent = (finalChat != null) ? finalChat.getContent() : null;
                    // 시간 계산 버그 수정
                    Duration timeSinceFinalChat = (finalChat != null) ? Duration.between(finalChat.getCreatedAt(), LocalDateTime.now()) : null;

                    return ChatroomResponse.of(
                            otherUserNickname,
                            finalChatContent,
                            timeSinceFinalChat
                    );
                })
                // 정렬 NPE 버그 수정
                .sorted(Comparator.comparing(ChatroomResponse::getAfterFinalChatTime, Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();

        // 메모리 내 페이지네이션 (DB 페이지네이션으로 개선 필요)
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), chatroomResponses.size());
        List<ChatroomResponse> subList = (start >= chatroomResponses.size()) ? List.of() : chatroomResponses.subList(start, end);
        boolean hasNext = end < chatroomResponses.size();

        return new SliceImpl<>(subList, pageable, hasNext);
    }

    @Override
    public Chatroom getChatroomById(Long chatroomId) {

        return chatroomRepository.findById(chatroomId).orElseThrow(
                () -> new ChatroomException(ChatroomErrorCode.NOT_EXIST_CHATROOM)
        );
    }
}
