package com.rental_api.ServiceBooking.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class WebSocketEventListener {

    private final SimpMessagingTemplate messagingTemplate;

    // email -> sessionId
    private static final Map<String, String> userSessionMap = new ConcurrentHashMap<>();

    // online users
    public static final Set<String> onlineUsers = ConcurrentHashMap.newKeySet();

    // last seen
    private static final Map<String, LocalDateTime> lastSeenMap = new ConcurrentHashMap<>();

    /* ================= CONNECT ================= */
    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {

        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());

        if (accessor.getUser() == null) return;

        String email = accessor.getUser().getName();
        String sessionId = accessor.getSessionId();

        if (email == null || sessionId == null) return;

        userSessionMap.put(email, sessionId);
        onlineUsers.add(email);

        PresenceMessage msg = new PresenceMessage(
                email,
                Status.ONLINE,
                null
        );

        messagingTemplate.convertAndSend("/topic/presence", msg);

        System.out.println("CONNECTED: " + email);
    }

    /* ================= DISCONNECT ================= */
    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {

        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = accessor.getSessionId();

        String email = userSessionMap.entrySet()
                .stream()
                .filter(e -> e.getValue().equals(sessionId))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);

        if (email == null) return;

        userSessionMap.remove(email);
        onlineUsers.remove(email);

        LocalDateTime lastSeen = LocalDateTime.now();
        lastSeenMap.put(email, lastSeen);

        PresenceMessage msg = new PresenceMessage(
                email,
                Status.OFFLINE,
                lastSeen
        );

        messagingTemplate.convertAndSend("/topic/presence", msg);

        System.out.println("DISCONNECTED: " + email);
    }

    /* ================= PRESENCE DTO ================= */
    public static class PresenceMessage {
        private String email;
        private Status status;
        private LocalDateTime lastSeen;

        public PresenceMessage(String email, Status status, LocalDateTime lastSeen) {
            this.email = email;
            this.status = status;
            this.lastSeen = lastSeen;
        }

        public String getEmail() { return email; }
        public Status getStatus() { return status; }
        public LocalDateTime getLastSeen() { return lastSeen; }
    }

    public enum Status {
        ONLINE,
        OFFLINE
    }
}