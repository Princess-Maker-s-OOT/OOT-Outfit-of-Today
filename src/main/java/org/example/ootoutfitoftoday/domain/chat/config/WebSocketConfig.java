package org.example.ootoutfitoftoday.domain.chat.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket : Spring Framework에서 지원하는 양방향 통신이 가능
 * 즉, 서버와 클라이언트가 지속적으로 연결되있는 것
 * <p>
 * STOMP 기반의 WebSocket 메시징 환경 설정 클래스.
 * 이 클래스는 Spring WebSocket Message Broker를 활성화하고,
 * 웹소켓 연결 엔드포인트, 메시지 송수신 경로(Prefix),
 * 그리고 클라이언트 인바운드 채널에 인터셉터(예: 인증 처리)를 등록합니다.
 */
@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final ChatInterceptor chatInterceptor;

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(chatInterceptor); //
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws/chatroom") // 클라이언트의 WebSocket 연결 엔드포인트
                .setAllowedOriginPatterns("*")
                .withSockJS(); // (옵션) SockJS 폴백
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 구독 경로 prefix
        registry.enableSimpleBroker("/topic", "/queue");

        // 메시지 송신 경로 prefix
        registry.setApplicationDestinationPrefixes("/app");
    }
}
