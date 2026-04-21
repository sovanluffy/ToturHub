package com.rental_api.ServiceBooking.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class WebSocketEventListener {

    private final SimpMessagingTemplate messagingTemplate;

    // email -> sessionId (multi-tab support)
    private static final Map<String, String> userSessionMap = new ConcurrentHashMap<>();

    // online users
    public static final Set<String> onlineUsers = ConcurrentHashMap.newKeySet();

    // optional: last seen tracking
    private static final Map<String, LocalDateTime> lastSeenMap = new ConcurrentHashMap<>();

    // ================= CONNECT =================
    @EventListener
    public void handleWebSocketConnectListener(SessionConnectEvent event) {

        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());

        if (accessor.getUser() != null) {

            String email = accessor.getUser().getName();
            String sessionId = accessor.getSessionId();

            userSessionMap.put(email, sessionId);
            onlineUsers.add(email);

            PresenceMessage msg = new PresenceMessage(
                    email,
                    "ONLINE",
                    null
            );

            messagingTemplate.convertAndSend("/topic/presence", msg);

            System.out.println("User Connected: " + email);
        }
    }

    // ================= DISCONNECT =================
    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {

        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());

        String sessionId = accessor.getSessionId();

        String emailToRemove = null;

        for (Map.Entry<String, String> entry : userSessionMap.entrySet()) {
            if (entry.getValue().equals(sessionId)) {
                emailToRemove = entry.getKey();
                break;
            }
        }

        if (emailToRemove != null) {

            userSessionMap.remove(emailToRemove);
            onlineUsers.remove(emailToRemove);

            // save last seen
            lastSeenMap.put(emailToRemove, LocalDateTime.now());

            PresenceMessage msg = new PresenceMessage(
                    emailToRemove,
                    "OFFLINE",
                    lastSeenMap.get(emailToRemove)
            );

            messagingTemplate.convertAndSend("/topic/presence", msg);

            System.out.println("User Disconnected: " + emailToRemove);
        }
    }

    // ================= PRESENCE DTO =================
    public static class PresenceMessage {

        private String email;
        private String status;
        private LocalDateTime lastSeen;

        public PresenceMessage(String email, String status, LocalDateTime lastSeen) {
            this.email = email;
            this.status = status;
            this.lastSeen = lastSeen;
        }

        public String getEmail() { return email; }
        public String getStatus() { return status; }
        public LocalDateTime getLastSeen() { return lastSeen; }
    }
}