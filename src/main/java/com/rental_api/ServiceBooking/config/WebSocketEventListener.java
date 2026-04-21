package com.rental_api.ServiceBooking.config; // Ensure this matches your folder structure

import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class WebSocketEventListener {

    private final SimpMessagingTemplate messagingTemplate;
    
    // A thread-safe Set to store the emails of users who are currently connected
    public static final Set<String> onlineUsers = Collections.synchronizedSet(new HashSet<>());

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        if (headerAccessor.getUser() != null) {
            String email = headerAccessor.getUser().getName();
            onlineUsers.add(email);
            
            // Broadcast to a public topic so everyone knows this user is online (Green Dot)
            messagingTemplate.convertAndSend("/topic/presence", email + ":ONLINE");
            System.out.println("User Connected: " + email);
        }
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        if (headerAccessor.getUser() != null) {
            String email = headerAccessor.getUser().getName();
            onlineUsers.remove(email);
            
            // Broadcast that the user went offline
            messagingTemplate.convertAndSend("/topic/presence", email + ":OFFLINE");
            System.out.println("User Disconnected: " + email);
        }
    }
}