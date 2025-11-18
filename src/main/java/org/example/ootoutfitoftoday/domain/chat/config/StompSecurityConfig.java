package org.example.ootoutfitoftoday.domain.chat.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.messaging.MessageSecurityMetadataSourceRegistry;
import org.springframework.security.config.annotation.web.socket.AbstractSecurityWebSocketMessageBrokerConfigurer;

@Configuration
public class StompSecurityConfig extends AbstractSecurityWebSocketMessageBrokerConfigurer {

    @Override
    protected void configureInbound(MessageSecurityMetadataSourceRegistry messages) {
        messages.nullDestMatcher().permitAll();

        messages.simpTypeMatchers(
                org.springframework.messaging.simp.SimpMessageType.CONNECT,
                org.springframework.messaging.simp.SimpMessageType.DISCONNECT,
                org.springframework.messaging.simp.SimpMessageType.UNSUBSCRIBE,
                org.springframework.messaging.simp.SimpMessageType.HEARTBEAT
        ).permitAll();

        messages.simpDestMatchers("/app/**").authenticated();

        messages.simpDestMatchers("/topic/**").authenticated();

        messages.anyMessage().denyAll();
    }

    @Override
    protected boolean sameOriginDisabled() {

        return true;
    }
}
