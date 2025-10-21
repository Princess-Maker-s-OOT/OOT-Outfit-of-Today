package org.example.ootoutfitoftoday.domain.chat.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.messaging.MessageSecurityMetadataSourceRegistry;
import org.springframework.security.config.annotation.web.socket.AbstractSecurityWebSocketMessageBrokerConfigurer;

/**
 * STOMP(WebSocket) 통신에 대한 Spring Security 설정 클래스.
 * * 웹소켓 연결 및 기본적인 명령은 허용하고,
 * 메시지 송신(/app/**) 및 구독(/topic/**) 경로에 대해서는
 * 반드시 인증된 사용자만 접근할 수 있도록 보안 정책을 정의합니다.
 * 또한, 토큰 기반 인증을 위해 CSRF 보호 기능을 비활성화합니다.
 */
@Configuration
public class StompSecurityConfig extends AbstractSecurityWebSocketMessageBrokerConfigurer { // ⭐ 클래스 이름 변경

    @Override
    protected void configureInbound(MessageSecurityMetadataSourceRegistry messages) {

        messages.nullDestMatcher().permitAll();

        messages.simpTypeMatchers(
                org.springframework.messaging.simp.SimpMessageType.CONNECT,
                org.springframework.messaging.simp.SimpMessageType.DISCONNECT,
                org.springframework.messaging.simp.SimpMessageType.UNSUBSCRIBE,
                org.springframework.messaging.simp.SimpMessageType.HEARTBEAT
        ).permitAll();

        // 메시지 전송 (Controller 경로)
        messages.simpDestMatchers("/app/**").authenticated();

        // 💡 메시지 구독 (Topic 경로) - 복수형 (s)로 수정
        messages.simpDestMatchers("/topic/**").authenticated();

        // 그 외 모든 메시지는 거부하여 보안 강화
        messages.anyMessage().denyAll();
    }

    // CSRF 토큰 검사 비활성화 (JWT 사용 시 HTTP 세션 미사용)
    @Override
    protected boolean sameOriginDisabled() {
        return true;
    }
}
