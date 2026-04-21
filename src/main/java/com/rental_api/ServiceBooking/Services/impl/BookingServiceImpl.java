package com.rental_api.ServiceBooking.Services.impl;

import com.rental_api.ServiceBooking.config.WebSocketEventListener;
import com.rental_api.ServiceBooking.Dto.ChatRequest;
import com.rental_api.ServiceBooking.Dto.ChatResponse;
import com.rental_api.ServiceBooking.Dto.NotificationMessage;
import com.rental_api.ServiceBooking.Dto.Request.BookingClassRequest;
import com.rental_api.ServiceBooking.Dto.Response.BookingResponse;
import com.rental_api.ServiceBooking.Entity.*;
import com.rental_api.ServiceBooking.Entity.Enum.BookingStatus;
import com.rental_api.ServiceBooking.Exception.ResourceNotFoundException;
import com.rental_api.ServiceBooking.Repository.*;
import com.rental_api.ServiceBooking.Services.BookingService;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final OpenClassRepository openClassRepository;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final DayTimeSlotRepository dayTimeSlotRepository;
    private final NotificationRepository notificationRepository;
    private final ChatMessageRepository chatMessageRepository; // NEW
    private final SimpMessagingTemplate messagingTemplate;

    // ================= CHAT: SEND MESSAGE =================
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
                .build();

        ChatMessage saved = chatMessageRepository.save(message);
        ChatResponse response = mapToChatResponse(saved);

        // Real-time push to recipient's private queue
        messagingTemplate.convertAndSendToUser(
                recipient.getEmail(),
                "/queue/messages",
                response
        );

        return response;
    }

    // ================= CHAT: GET HISTORY =================
    @Override
    public List<ChatResponse> getChatHistory(String myEmail, Long otherUserId) {
        User me = userRepository.findByEmail(myEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return chatMessageRepository.findChatHistory(me.getId(), otherUserId)
                .stream()
                .map(this::mapToChatResponse)
                .toList();
    }

    // ================= CHAT: MARK AS SEEN =================
    @Override
    @Transactional
    public void markMessagesAsRead(String recipientEmail, Long senderId) {
        User me = userRepository.findByEmail(recipientEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        List<ChatMessage> unread = chatMessageRepository
                .findByRecipient_IdAndSender_IdAndIsReadFalse(me.getId(), senderId);

        if (!unread.isEmpty()) {
            unread.forEach(m -> m.setRead(true));
            chatMessageRepository.saveAll(unread);

            // Notify the sender that the message was seen
            User sender = userRepository.findById(senderId).orElse(null);
            if (sender != null) {
                messagingTemplate.convertAndSendToUser(
                        sender.getEmail(),
                        "/queue/seen-notifications",
                        me.getId()
                );
            }
        }
    }

    // ================= CORE BOOKING LOGIC =================
    @Override
    @Transactional
    public BookingResponse bookClass(Long openClassId, BookingClassRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User student = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        OpenClass openClass = openClassRepository.findById(openClassId)
                .orElseThrow(() -> new ResourceNotFoundException("Class not found"));

        DayTimeSlot slot = dayTimeSlotRepository.findByIdForUpdate(request.getDayTimeSlotId())
                .orElseThrow(() -> new ResourceNotFoundException("Schedule not found"));

        if (!slot.getOpenClass().getId().equals(openClassId)) {
            throw new IllegalStateException("Schedule does not belong to this class");
        }

        if (bookingRepository.existsByUser_IdAndSchedule_Id(student.getId(), slot.getId())) {
            throw new IllegalStateException("Already booked!");
        }

        int booked = slot.getBookedCount() == null ? 0 : slot.getBookedCount();
        int max = slot.getMaxStudents() == null ? 10 : slot.getMaxStudents();
        if (booked >= max) throw new IllegalStateException("Full slot!");

        slot.setBookedCount(booked + 1);

        BookingClass booking = BookingClass.builder()
                .user(student)
                .tutor(openClass.getTutor())
                .openClass(openClass)
                .schedule(slot)
                .telegram(request.getTelegram())
                .note(request.getNote())
                .status(BookingStatus.PENDING)
                .build();

        BookingClass saved = bookingRepository.save(booking);

        sendNotification(
                openClass.getTutor().getUser().getEmail(),
                "BOOKING_REQUEST",
                student.getFullname() + " requested " + openClass.getTitle(),
                saved.getId(), openClass.getId(), student, openClass, slot, request.getTelegram()
        );

        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public BookingResponse confirmBooking(Long bookingId) {
        BookingClass booking = bookingRepository.findById(bookingId).orElseThrow();
        booking.setStatus(BookingStatus.CONFIRMED);
        return mapToResponse(bookingRepository.save(booking));
    }

    @Override
    @Transactional
    public BookingResponse rejectBooking(Long bookingId) {
        BookingClass booking = bookingRepository.findById(bookingId).orElseThrow();
        booking.setStatus(BookingStatus.REJECTED);
        return mapToResponse(bookingRepository.save(booking));
    }

    // ================= GETTERS & COUNTS =================
    @Override
    public List<BookingResponse> getBookingsByUserId(Long userId) {
        return bookingRepository.findByUser_Id(userId).stream().map(this::mapToResponse).toList();
    }

    @Override
    public List<BookingResponse> getBookingsByClassId(Long classId) {
        return bookingRepository.findByOpenClass_Id(classId).stream().map(this::mapToResponse).toList();
    }

    @Override
    public List<BookingResponse> getBookingsByTutorId(Long tutorId) {
        return bookingRepository.findByTutor_Id(tutorId).stream().map(this::mapToResponse).toList();
    }

    @Override
    public List<BookingResponse> getMyBookings() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User student = userRepository.findByEmail(email).orElseThrow();
        return bookingRepository.findByUser_Id(student.getId()).stream().map(this::mapToResponse).toList();
    }

    @Override
    public List<BookingResponse> getMyTutorBookings() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User tutor = userRepository.findByEmail(email).orElseThrow();
        return bookingRepository.findByTutor_Id(tutor.getId()).stream().map(this::mapToResponse).toList();
    }

    @Override
    public Long getMyPendingBookingsCount() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User tutor = userRepository.findByEmail(email).orElseThrow();
        return bookingRepository.countByTutor_IdAndStatus(tutor.getId(), BookingStatus.PENDING);
    }

    // ================= MAPPERS =================
    private ChatResponse mapToChatResponse(ChatMessage m) {
        ChatResponse res = new ChatResponse();
        res.setId(m.getId());
        res.setSenderId(m.getSender().getId());
        res.setSenderName(m.getSender().getFullname());
        res.setSenderAvatar(m.getSender().getAvatarUrl());
        res.setRecipientId(m.getRecipient().getId());
        res.setContent(m.getContent());
        res.setTimestamp(m.getTimestamp());
        res.setRead(m.isRead());
        // ONLINE CHECK
        res.setOnline(WebSocketEventListener.onlineUsers.contains(m.getRecipient().getEmail()));
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

    // ================= NOTIFICATION HELPER =================
    private void sendNotification(String email, String type, String content, Long bId, Long cId, User student, OpenClass o, DayTimeSlot s, String tel) {
        Notification n = Notification.builder().recipientEmail(email).type(type).content(content).bookingId(bId).classId(cId).isRead(false).build();
        notificationRepository.save(n);
        NotificationMessage msg = new NotificationMessage();
        msg.setType(type); msg.setContent(content); msg.setBookingId(bId); msg.setClassId(cId);
        msg.setUserId(student.getId()); msg.setFullname(student.getFullname()); msg.setEmail(student.getEmail());
        msg.setPhone(student.getPhone()); msg.setAvatarUrl(student.getAvatarUrl());
        msg.setTutorId(o.getTutor() != null ? o.getTutor().getId() : null);
        msg.setStudentName(student.getFullname()); msg.setClassTitle(o.getTitle());
        msg.setDay(s.getDay().toString()); msg.setStartTime(s.getStartTime().toString()); msg.setEndTime(s.getEndTime().toString());
        msg.setTelegram(tel);
        messagingTemplate.convertAndSendToUser(email, "/queue/notifications", msg);
    }
}