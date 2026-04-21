package com.rental_api.ServiceBooking.Services.impl;

import com.rental_api.ServiceBooking.Dto.ChatRequest;
import com.rental_api.ServiceBooking.Dto.ChatResponse;
import com.rental_api.ServiceBooking.Dto.Request.BookingClassRequest;
import com.rental_api.ServiceBooking.Dto.Response.BookingResponse;
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
    private final SimpMessagingTemplate messagingTemplate;

    /* ================= BOOKING ================= */

    @Override
    @Transactional
    public BookingResponse bookClass(Long openClassId, BookingClassRequest request) {

        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        User student = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

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

        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public BookingResponse confirmBooking(Long bookingId) {

        BookingClass booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        booking.setStatus(BookingStatus.CONFIRMED);
        BookingClass saved = bookingRepository.save(booking);

        sendSystemMessage(
                booking.getTutor().getUser(),
                booking.getUser(),
                "🎉 Your booking for '" + booking.getOpenClass().getTitle() + "' is CONFIRMED"
        );

        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public BookingResponse rejectBooking(Long bookingId) {

        BookingClass booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        booking.setStatus(BookingStatus.REJECTED);
        BookingClass saved = bookingRepository.save(booking);

        sendSystemMessage(
                booking.getTutor().getUser(),
                booking.getUser(),
                "❌ Your booking for '" + booking.getOpenClass().getTitle() + "' is REJECTED"
        );

        return mapToResponse(saved);
    }

    /* ================= BOOKINGS LIST ================= */

    @Override
    public List<BookingResponse> getBookingsByUserId(Long userId) {
        return bookingRepository.findByUser_Id(userId)
                .stream().map(this::mapToResponse).toList();
    }

    @Override
    public List<BookingResponse> getBookingsByClassId(Long classId) {
        return bookingRepository.findByOpenClass_Id(classId)
                .stream().map(this::mapToResponse).toList();
    }

    @Override
    public List<BookingResponse> getBookingsByTutorId(Long tutorId) {
        return bookingRepository.findByTutor_Id(tutorId)
                .stream().map(this::mapToResponse).toList();
    }

    @Override
    public List<BookingResponse> getMyBookings() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email).orElseThrow();
        return bookingRepository.findByUser_Id(user.getId())
                .stream().map(this::mapToResponse).toList();
    }

    @Override
    public List<BookingResponse> getMyTutorBookings() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User tutor = userRepository.findByEmail(email).orElseThrow();
        return bookingRepository.findByTutor_Id(tutor.getId())
                .stream().map(this::mapToResponse).toList();
    }

    @Override
    public Long getMyPendingBookingsCount() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User tutor = userRepository.findByEmail(email).orElseThrow();
        return bookingRepository.countByTutor_IdAndStatus(tutor.getId(), BookingStatus.PENDING);
    }

    /* ================= CHAT ================= */

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
                .isRead(false)
                .timestamp(LocalDateTime.now())
                .type(MessageType.USER)
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

    @Override
    public List<ChatResponse> getChatHistory(String myEmail, Long otherUserId) {

        User me = userRepository.findByEmail(myEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return chatMessageRepository.findChatHistory(me.getId(), otherUserId)
                .stream()
                .map(this::mapToChatResponse)
                .toList();
    }

    @Override
    @Transactional
    public void markMessagesAsRead(String recipientEmail, Long senderId) {

        User me = userRepository.findByEmail(recipientEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        List<ChatMessage> unread =
                chatMessageRepository.findByRecipient_IdAndSender_IdAndIsReadFalse(
                        me.getId(), senderId
                );

        unread.forEach(m -> m.setRead(true));
        chatMessageRepository.saveAll(unread);
    }

    /* ================= SYSTEM CHAT MESSAGE ================= */

    private void sendSystemMessage(User sender, User recipient, String content) {

        ChatMessage message = ChatMessage.builder()
                .sender(sender)
                .recipient(recipient)
                .content(content)
                .isRead(false)
                .timestamp(LocalDateTime.now())
                .type(MessageType.SYSTEM)
                .build();

        ChatMessage saved = chatMessageRepository.save(message);

        ChatResponse response = mapToChatResponse(saved);

        messagingTemplate.convertAndSendToUser(
                recipient.getEmail(),
                "/queue/messages",
                response
        );
    }

    /* ================= MAPPERS ================= */

    private ChatResponse mapToChatResponse(ChatMessage m) {

        ChatResponse res = new ChatResponse();
        res.setId(m.getId());
        res.setSenderId(m.getSender().getId());
        res.setRecipientId(m.getRecipient().getId());
        res.setContent(m.getContent());
        res.setTimestamp(m.getTimestamp());
        res.setRead(m.isRead());
        res.setMessageType(m.getType().name());
        return res;
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
}