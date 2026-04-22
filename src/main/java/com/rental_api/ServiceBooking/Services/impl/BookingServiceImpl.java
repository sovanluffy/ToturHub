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
import java.util.Map;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Set;

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

    // ================= BOOKING =================

    @Override
    @Transactional
    public BookingResponse bookClass(Long openClassId, BookingClassRequest request) {

        User student = getCurrentUser();

        OpenClass openClass = openClassRepository.findById(openClassId)
                .orElseThrow(() -> new ResourceNotFoundException("Class not found"));

        DayTimeSlot slot = dayTimeSlotRepository.findById(request.getDayTimeSlotId())
                .orElseThrow(() -> new ResourceNotFoundException("Schedule not found"));

        // ================= RULE: BLOCK SAME SCHEDULE =================
        List<BookingStatus> blockedStatuses = List.of(
                BookingStatus.PENDING,
                BookingStatus.CONFIRMED
        );

        boolean exists = bookingRepository
                .existsByUser_IdAndSchedule_IdAndStatusIn(
                        student.getId(),
                        slot.getId(),
                        blockedStatuses
                );

        if (exists) {
            throw new RuntimeException(
                    "You already have a PENDING or CONFIRMED booking for this schedule"
            );
        }

        // ================= CREATE BOOKING =================
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

        BookingClass booking = bookingRepository.findByIdWithDetails(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        booking.setStatus(BookingStatus.CONFIRMED);

        BookingClass saved = bookingRepository.save(booking);

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

        BookingClass booking = bookingRepository.findByIdWithDetails(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        booking.setStatus(BookingStatus.REJECTED);

        BookingClass saved = bookingRepository.save(booking);

        sendSystemChat(
                tutorUser,
                saved.getUser(),
                "❌ Booking REJECTED: " + saved.getOpenClass().getTitle()
        );

        return mapToResponse(saved);
    }

    // ================= GET BOOKINGS =================

    @Override
    public List<BookingResponse> getBookingsByUserId(Long userId) {
        return bookingRepository.findAllByUserWithDetails(userId)
                .stream().map(this::mapToResponse).toList();
    }

    @Override
    public List<BookingResponse> getBookingsByClassId(Long classId) {
        return bookingRepository.findByOpenClass_Id(classId)
                .stream().map(this::mapToResponse).toList();
    }

    @Override
    public List<BookingResponse> getBookingsByTutorId(Long tutorId) {
        return bookingRepository.findAllByTutorWithDetails(tutorId)
                .stream().map(this::mapToResponse).toList();
    }

    @Override
    public List<BookingResponse> getMyBookings() {
        User user = getCurrentUser();
        return bookingRepository.findAllByUserWithDetails(user.getId())
                .stream().map(this::mapToResponse).toList();
    }

    @Override
    public List<BookingResponse> getMyTutorBookings() {
        User user = getCurrentUser();
        Tutor tutor = getTutor(user);
        return bookingRepository.findAllByTutorWithDetails(tutor.getId())
                .stream().map(this::mapToResponse).toList();
    }

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
    public void markMessagesAsRead(String recipientEmail, Long senderId) {

        User me = getCurrentUser();

        List<ChatMessage> unread =
                chatMessageRepository.findByRecipient_IdAndSender_IdAndIsReadFalse(
                        me.getId(), senderId
                );

        unread.forEach(m -> m.setRead(true));
        chatMessageRepository.saveAll(unread);
    }

    // ================= CHAT CONTACTS =================

    @Override
    public List<ChatContactResponse> getChatContacts(String email) {

        User me = getCurrentUser();

        List<ChatMessage> messages =
                chatMessageRepository.findAllUserMessages(me.getId());

        Map<Long, ChatMessage> lastMessageMap = new HashMap<>();
        Map<Long, Long> unreadMap = new HashMap<>();

        for (ChatMessage m : messages) {

            Long otherId = m.getSender().getId().equals(me.getId())
                    ? m.getRecipient().getId()
                    : m.getSender().getId();

            lastMessageMap.putIfAbsent(otherId, m);

            if (!m.isRead() && m.getRecipient().getId().equals(me.getId())) {
                unreadMap.put(otherId,
                        unreadMap.getOrDefault(otherId, 0L) + 1);
            }
        }

        return lastMessageMap.entrySet().stream()
                .map(entry -> {

                    User other = userRepository.findById(entry.getKey()).orElse(null);
                    ChatMessage last = entry.getValue();

                    ChatContactResponse res = new ChatContactResponse();
                    res.setUserId(entry.getKey());
                    res.setName(other != null ? other.getFullname() : "Unknown");
                    res.setAvatar(other != null ? other.getAvatarUrl() : null);
                    res.setLastMessage(last.getContent());
                    res.setLastTime(last.getTimestamp().toString());
                    res.setUnreadCount(unreadMap.getOrDefault(entry.getKey(), 0L));

                    return res;
                })
                .toList();
    }

    @Override
    public List<Long> getChatUserList(String email) {

        User me = getCurrentUser();

        List<ChatMessage> messages =
                chatMessageRepository.findAllUserMessages(me.getId());

        Set<Long> ids = new LinkedHashSet<>();

        for (ChatMessage m : messages) {
            if (m.getSender().getId().equals(me.getId())) {
                ids.add(m.getRecipient().getId());
            } else {
                ids.add(m.getSender().getId());
            }
        }

        return List.copyOf(ids);
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

        chatMessageRepository.save(message);
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
        return r;
    }

    private BookingResponse mapToResponse(BookingClass b) {

        User user = b.getUser();
        OpenClass openClass = b.getOpenClass();
        DayTimeSlot slot = b.getSchedule();

        return BookingResponse.builder()
                .bookingId(b.getId())

                .userId(user.getId())
                .studentName(user.getFullname())
                .studentEmail(user.getEmail())
                .studentPhone(user.getPhone())
                .studentAvatar(user.getAvatarUrl())

                .classId(openClass != null ? openClass.getId() : null)
                .classTitle(openClass != null ? openClass.getTitle() : null)

                .scheduleId(slot != null ? slot.getId() : null)
                .day(slot != null ? slot.getDay() : null)
                .startTime(slot != null ? slot.getStartTime() : null)
                .endTime(slot != null ? slot.getEndTime() : null)

                .status(b.getStatus())
                .note(b.getNote())
                .telegram(b.getTelegram())
                .createdAt(b.getCreatedAt())

                .build();
    }
}