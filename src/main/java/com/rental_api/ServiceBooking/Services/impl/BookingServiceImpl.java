package com.rental_api.ServiceBooking.Services.impl;

import com.rental_api.ServiceBooking.Dto.ChatRequest;
import com.rental_api.ServiceBooking.Dto.ChatResponse;
import com.rental_api.ServiceBooking.Dto.Request.BookingClassRequest;
import com.rental_api.ServiceBooking.Dto.Response.BookingResponse;
import com.rental_api.ServiceBooking.Dto.NotificationMessage;
import com.rental_api.ServiceBooking.Entity.*;
import com.rental_api.ServiceBooking.Entity.Enum.BookingStatus;
import com.rental_api.ServiceBooking.Entity.Enum.MessageType;
import com.rental_api.ServiceBooking.Exception.ResourceNotFoundException;
import com.rental_api.ServiceBooking.Repository.*;
import com.rental_api.ServiceBooking.Services.BookingService;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final OpenClassRepository openClassRepository;
    private final DayTimeSlotRepository dayTimeSlotRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final NotificationRepository notificationRepository;
    private final SimpMessagingTemplate messagingTemplate;

    /* ================= BOOK CLASS ================= */
    @Override
    @Transactional
    public BookingResponse bookClass(Long openClassId, BookingClassRequest request) {

        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        User student = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));

        OpenClass openClass = openClassRepository.findById(openClassId)
                .orElseThrow(() -> new ResourceNotFoundException("Class not found"));

        DayTimeSlot slot = dayTimeSlotRepository.findById(request.getDayTimeSlotId())
                .orElseThrow(() -> new ResourceNotFoundException("Schedule not found"));

        BookingClass booking = BookingClass.builder()
                .user(student)
                .tutor(openClass.getTutor())
                .openClass(openClass)
                .schedule(slot)
                .telegram(request.getTelegram())
                .note(request.getNote())
                .status(BookingStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();

        BookingClass saved = bookingRepository.save(booking);

        // 👉 SEND CHAT MESSAGE TO TUTOR
        sendSystemChat(
                student,
                openClass.getTutor().getUser(),
                "📌 New booking request from " + student.getFullname() +
                        " for class: " + openClass.getTitle()
        );

        return mapToResponse(saved);
    }

    /* ================= CONFIRM ================= */
    @Override
    @Transactional
    public BookingResponse confirmBooking(Long bookingId) {

        BookingClass booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        booking.setStatus(BookingStatus.CONFIRMED);
        BookingClass saved = bookingRepository.save(booking);

        // 👉 CHAT MESSAGE TO STUDENT
        sendSystemChat(
                booking.getTutor().getUser(),
                booking.getUser(),
                "🎉 Your booking is CONFIRMED for " +
                        booking.getOpenClass().getTitle()
        );

        return mapToResponse(saved);
    }

    /* ================= REJECT ================= */
    @Override
    @Transactional
    public BookingResponse rejectBooking(Long bookingId) {

        BookingClass booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        booking.setStatus(BookingStatus.REJECTED);
        BookingClass saved = bookingRepository.save(booking);

        // 👉 CHAT MESSAGE TO STUDENT
        sendSystemChat(
                booking.getTutor().getUser(),
                booking.getUser(),
                "❌ Your booking is REJECTED for " +
                        booking.getOpenClass().getTitle()
        );

        return mapToResponse(saved);
    }
@Override
public List<ChatResponse> getChatHistory(String myEmail, Long otherUserId) {

    User me = userRepository.findByEmail(myEmail)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

    List<ChatMessage> messages =
            chatMessageRepository.findChatHistory(me.getId(), otherUserId);

    return messages.stream()
            .map(this::mapToChatResponse)
            .toList();
}
    /* ================= SYSTEM CHAT (MAIN LOGIC) ================= */
    private void sendSystemChat(User sender, User recipient, String content) {

        ChatMessage message = ChatMessage.builder()
                .sender(sender)
                .recipient(recipient)
                .content(content)
                .type(MessageType.SYSTEM)
                .isRead(false)
                .timestamp(LocalDateTime.now())
                .build();

        ChatMessage saved = chatMessageRepository.save(message);

        ChatResponse response = mapToChatResponse(saved);

        // 👉 REAL CHAT STREAM
        messagingTemplate.convertAndSendToUser(
                recipient.getEmail(),
                "/queue/messages",
                response
        );

        // 👉 OPTIONAL: NOTIFICATION (BELL UI)
        NotificationMessage notification = new NotificationMessage();
        notification.setType("BOOKING_CHAT");
        notification.setContent(content);
        notification.setUserId(recipient.getId());

        messagingTemplate.convertAndSendToUser(
                recipient.getEmail(),
                "/queue/notifications",
                notification
        );
    }

    /* ================= CHAT NORMAL USER MESSAGE ================= */
    @Override
    @Transactional
    public ChatResponse sendMessage(String senderEmail, ChatRequest request) {

        User sender = userRepository.findByEmail(senderEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Sender not found"));

        User recipient = userRepository.findById(request.getRecipientId())
                .orElseThrow(() -> new ResourceNotFoundException("Recipient not found"));

        ChatMessage message = ChatMessage.builder()
                .sender(sender)
                .recipient(recipient)
                .content(request.getContent())
                .type(MessageType.USER)
                .isRead(false)
                .timestamp(LocalDateTime.now())
                .build();

        ChatMessage saved = chatMessageRepository.save(message);

        ChatResponse response = mapToChatResponse(saved);

        messagingTemplate.convertAndSendToUser(
                recipient.getEmail(),
                "/queue/messages",
                response
        );

        return response;
    }

    /* ================= READ CHAT ================= */
    @Override
    @Transactional
    public void markMessagesAsRead(String email, Long senderId) {

        User me = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        List<ChatMessage> unread =
                chatMessageRepository.findByRecipient_IdAndSender_IdAndIsReadFalse(
                        me.getId(), senderId
                );

        unread.forEach(m -> m.setRead(true));
        chatMessageRepository.saveAll(unread);
    }

    /* ================= MAPPERS ================= */

    private ChatResponse mapToChatResponse(ChatMessage m) {
        ChatResponse r = new ChatResponse();
        r.setId(m.getId());
        r.setSenderId(m.getSender().getId());
        r.setRecipientId(m.getRecipient().getId());
        r.setContent(m.getContent());
        r.setTimestamp(m.getTimestamp());
        r.setRead(m.isRead());
        r.setMessageType(m.getType().name());
        return r;
    }

    private BookingResponse mapToResponse(BookingClass b) {
        return BookingResponse.builder()
                .bookingId(b.getId())
                .userId(b.getUser().getId())
                .studentName(b.getUser().getFullname())
                .studentEmail(b.getUser().getEmail())
                .studentPhone(b.getUser().getPhone())
                .studentAvatar(b.getUser().getAvatarUrl())
                .classId(b.getOpenClass().getId())
                .classTitle(b.getOpenClass().getTitle())
                .scheduleId(b.getSchedule().getId())
                .day(b.getSchedule().getDay())
                .startTime(b.getSchedule().getStartTime())
                .endTime(b.getSchedule().getEndTime())
                .status(b.getStatus())
                .note(b.getNote())
                .telegram(b.getTelegram())
                .createdAt(b.getCreatedAt())
                .build();
    }

    /* ================= UNUSED LIST METHODS ================= */
    @Override public List<BookingResponse> getBookingsByUserId(Long userId){return List.of();}
    @Override public List<BookingResponse> getBookingsByClassId(Long classId){return List.of();}
    @Override public List<BookingResponse> getBookingsByTutorId(Long tutorId){return List.of();}
    @Override public List<BookingResponse> getMyBookings(){return List.of();}
    @Override public List<BookingResponse> getMyTutorBookings(){return List.of();}
    @Override public Long getMyPendingBookingsCount(){return 0L;}
}