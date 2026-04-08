package com.rental_api.ServiceBooking.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Messages from server to client
        config.enableSimpleBroker("/topic", "/queue");
        // Messages from client to server
        config.setApplicationDestinationPrefixes("/app");
        // For private messages (one-to-one)
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // The endpoint used for connection (with SockJS fallback)
        registry.addEndpoint("/ws-booking").setAllowedOriginPatterns("*").withSockJS();
    }
}