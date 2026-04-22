package com.rental_api.ServiceBooking.Services.impl;

import com.rental_api.ServiceBooking.Dto.*;
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

    // ================= UTIL =================
    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private Tutor getTutor(User user) {
        if (user.getTutor() == null) {
            throw new RuntimeException("User is not a tutor");
        }
        return user.getTutor();
    }

    // ================= BOOK CLASS =================
    @Override
    @Transactional
    public BookingResponse bookClass(Long openClassId, BookingClassRequest request) {

        User student = getCurrentUser();

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

        sendSystemChat(
                student,
                openClass.getTutor().getUser(),
                "📌 New booking from " + student.getFullname()
        );

        return mapToResponse(saved);
    }

    // ================= CONFIRM =================
    @Override
    @Transactional
    public BookingResponse confirmBooking(Long bookingId) {

        User tutorUser = getCurrentUser();

        BookingClass booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        booking.setStatus(BookingStatus.CONFIRMED);

        BookingClass saved = bookingRepository.save(booking);

        // ================= REAL-TIME NOTIFICATION =================
        sendBookingNotification(saved, "CONFIRMED", "🎉 Your booking has been CONFIRMED");

        sendSystemChat(
                tutorUser,
                saved.getUser(),
                "🎉 Booking CONFIRMED: " + saved.getOpenClass().getTitle()
        );

        return mapToResponse(saved);
    }

    // ================= REJECT =================
    @Override
    @Transactional
    public BookingResponse rejectBooking(Long bookingId) {

        User tutorUser = getCurrentUser();

        BookingClass booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        booking.setStatus(BookingStatus.REJECTED);

        BookingClass saved = bookingRepository.save(booking);

        // ================= REAL-TIME NOTIFICATION =================
        sendBookingNotification(saved, "REJECTED", "❌ Your booking has been REJECTED");

        sendSystemChat(
                tutorUser,
                saved.getUser(),
                "❌ Booking REJECTED: " + saved.getOpenClass().getTitle()
        );

        return mapToResponse(saved);
    }

    // ================= REAL-TIME NOTIFICATION METHOD =================
    private void sendBookingNotification(BookingClass booking, String status, String message) {

        BookingNotification notif = BookingNotification.builder()
                .type("BOOKING_UPDATE")
                .bookingId(booking.getId())
                .status(status)
                .message(message)
                .tutorId(booking.getTutor().getId())
                .studentId(booking.getUser().getId())
                .timestamp(LocalDateTime.now())
                .build();

        messagingTemplate.convertAndSendToUser(
                booking.getUser().getEmail(),
                "/queue/notifications",
                notif
        );
    }

    // ================= GET BOOKINGS =================
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

    // ================= MY BOOKINGS =================
    @Override
    public List<BookingResponse> getMyBookings() {
        User user = getCurrentUser();
        return bookingRepository.findByUser_Id(user.getId())
                .stream().map(this::mapToResponse).toList();
    }

    @Override
    public List<BookingResponse> getMyTutorBookings() {
        User user = getCurrentUser();
        Tutor tutor = getTutor(user);
        return bookingRepository.findByTutor_Id(tutor.getId())
                .stream().map(this::mapToResponse).toList();
    }

    // ================= COUNT =================
    @Override
    public Long getMyPendingBookingsCount() {
        User user = getCurrentUser();

        if (user.getTutor() == null) return 0L;

        return bookingRepository.countByTutor_IdAndStatus(
                user.getTutor().getId(),
                BookingStatus.PENDING
        );
    }

    // ================= CHAT =================
    @Override
    public List<ChatResponse> getChatHistory(String email, Long otherUserId) {
        User me = getCurrentUser();

        return chatMessageRepository.findChatHistory(me.getId(), otherUserId)
                .stream()
                .map(this::mapToChatResponse)
                .toList();
    }

    @Override
    public Long getUnreadMessageCount(String email) {
        User me = getCurrentUser();
        return chatMessageRepository.countUnreadByRecipient(me.getId());
    }

    @Override
    @Transactional
    public void markMessagesAsRead(String email, Long senderId) {
        User me = getCurrentUser();

        List<ChatMessage> unread =
                chatMessageRepository.findByRecipient_IdAndSender_IdAndIsReadFalse(
                        me.getId(), senderId
                );

        unread.forEach(m -> m.setRead(true));
        chatMessageRepository.saveAll(unread);
    }

    @Override
    @Transactional
    public ChatResponse sendMessage(String senderEmail, ChatRequest request) {

        User sender = getCurrentUser();

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

    // ================= SYSTEM CHAT =================
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

        messagingTemplate.convertAndSendToUser(
                recipient.getEmail(),
                "/queue/messages",
                mapToChatResponse(saved)
        );
    }

    // ================= MAPPERS =================
    private ChatResponse mapToChatResponse(ChatMessage m) {

        ChatResponse r = new ChatResponse();

        r.setId(m.getId());
        r.setSenderId(m.getSender().getId());
        r.setRecipientId(m.getRecipient().getId());
        r.setContent(m.getContent());
        r.setTimestamp(m.getTimestamp());
        r.setRead(m.isRead());
        r.setMessageType(m.getType().name());

        if (m.getBooking() != null) {
            r.setBookingId(m.getBooking().getId());
            r.setBookingStatus(m.getBooking().getStatus().name());
        }

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
}