package dev.dunglv202.hoaithuong.config;

import dev.dunglv202.hoaithuong.helper.JwtProvider;
import dev.dunglv202.hoaithuong.model.auth.AuthHandshakeInterceptor;
import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.List;

import static dev.dunglv202.hoaithuong.config.SecurityConfig.ACCESS_TOKEN_COOKIE;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    public static final String TOKEN_ATTRIBUTE = "token";

    private final JwtProvider jwtProvider;

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws").setAllowedOriginPatterns("*")
            .withSockJS()
            .setInterceptors(authHandshakeInterceptor());
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        /* STOMP messages whose destination header begins with /app are routed to @MessageMapping methods in @Controller */
        config.setApplicationDestinationPrefixes("/app");
        config.enableSimpleBroker("/topic", "/queue");
    }

    @Override
    public void configureClientInboundChannel(@Nonnull ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(@Nonnull Message<?> message, @Nonnull MessageChannel channel) {
                StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

                // if not connect step => skip
                if (accessor == null || accessor.getCommand() != StompCommand.CONNECT || accessor.getSessionAttributes() == null) {
                    return message;
                }

                // extract token set in the interceptor then verify
                String token = (String) accessor.getSessionAttributes().get(ACCESS_TOKEN_COOKIE);
                if (token != null) {
                    String userId = jwtProvider.verifyToken(token).getSubject();
                    var authenticationToken = new PreAuthenticatedAuthenticationToken(
                        userId,
                        null,
                        List.of()
                    );
                    accessor.setUser(authenticationToken);
                }

                return message;
            }
        });
    }

    @Bean
    public HandshakeInterceptor authHandshakeInterceptor() {
        return new AuthHandshakeInterceptor();
    }
}
