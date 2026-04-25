package com.rental_api.ServiceBooking.Services.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
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

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final OpenClassRepository openClassRepository;
    private final DayTimeSlotRepository dayTimeSlotRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final Cloudinary cloudinary;

    // =====================================================
    // ================= UTIL ==============================
    // =====================================================

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

    // =====================================================
    // ================= BOOKING ===========================
    // =====================================================

    @Override
    @Transactional
    public BookingResponse bookClass(Long openClassId, BookingClassRequest request) {

        User student = getCurrentUser();

        OpenClass openClass = openClassRepository.findById(openClassId)
                .orElseThrow(() -> new ResourceNotFoundException("Class not found"));

        DayTimeSlot slot = dayTimeSlotRepository.findById(request.getDayTimeSlotId())
                .orElseThrow(() -> new ResourceNotFoundException("Schedule not found"));

        boolean exists = bookingRepository.existsByUser_IdAndSchedule_IdAndStatusIn(
                student.getId(),
                slot.getId(),
                List.of(BookingStatus.PENDING, BookingStatus.CONFIRMED)
        );

        if (exists) {
            throw new RuntimeException("Already booked this schedule");
        }

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

        sendSystemChat(student, openClass.getTutor().getUser(),
                "📌 New booking from " + student.getFullname());

        return mapToResponse(saved);
    }

    @Override
    public BookingResponse confirmBooking(Long bookingId) {

        User tutorUser = getCurrentUser();

        BookingClass booking = bookingRepository.findByIdWithDetails(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        booking.setStatus(BookingStatus.CONFIRMED);

        return mapToResponse(bookingRepository.save(booking));
    }

    @Override
    public BookingResponse rejectBooking(Long bookingId) {

        BookingClass booking = bookingRepository.findByIdWithDetails(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        booking.setStatus(BookingStatus.REJECTED);

        return mapToResponse(bookingRepository.save(booking));
    }

    // =====================================================
    // ================= REQUIRED SERVICE METHODS ==========
    // =====================================================

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

    // =====================================================
    // ================= CHAT + IMAGE + CLOUDINARY =========
    // =====================================================

    @Override
    public ChatResponse sendMessage(String senderEmail, ChatRequest request) {

        User sender = getCurrentUser();

        User recipient = userRepository.findById(request.getRecipientId())
                .orElseThrow(() -> new ResourceNotFoundException("Recipient not found"));

        String mediaUrl = null;

        // ✅ CLOUDINARY UPLOAD
        if (request.getFile() != null && !request.getFile().isEmpty()) {
            try {
                Map upload = cloudinary.uploader().upload(
                        request.getFile().getBytes(),
                        ObjectUtils.asMap(
                                "folder", "chat_media",
                                "resource_type", "auto"
                        )
                );
                mediaUrl = upload.get("secure_url").toString();
            } catch (IOException e) {
                throw new RuntimeException("Cloudinary upload failed");
            }
        }

        MessageType type = request.hasFile()
                ? request.resolveType()
                : MessageType.USER;

        ChatMessage message = ChatMessage.builder()
                .sender(sender)
                .recipient(recipient)
                .content(request.getContent())
                .type(type)
                .mediaUrl(mediaUrl)
                .booking(null)
                .read(false)
                .timestamp(Instant.now())
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
        User me = getCurrentUser();

        return chatMessageRepository.findChatHistory(me.getId(), otherUserId)
                .stream().map(this::mapToChatResponse).toList();
    }

    @Override
    public void markMessagesAsRead(String recipientEmail, Long senderId) {

        User me = getCurrentUser();

        List<ChatMessage> unread =
                chatMessageRepository.findByRecipient_IdAndSender_IdAndReadFalse(
                        me.getId(), senderId
                );

        unread.forEach(m -> m.setRead(true));
        chatMessageRepository.saveAll(unread);
    }

    @Override
    public Long getUnreadMessageCount(String email) {
        User me = getCurrentUser();
        return chatMessageRepository.countUnreadByRecipient(me.getId());
    }

    // =====================================================
    // ================= CONTACTS ==========================
    // =====================================================

    @Override
    public List<ChatContactResponse> getChatContacts(String email) {
        User me = getCurrentUser();

        List<ChatMessage> messages = chatMessageRepository.findAllUserMessages(me.getId());

        Map<Long, ChatMessage> last = new HashMap<>();
        Map<Long, Long> unread = new HashMap<>();

        for (ChatMessage m : messages) {
            Long otherId = m.getSender().getId().equals(me.getId())
                    ? m.getRecipient().getId()
                    : m.getSender().getId();

            last.putIfAbsent(otherId, m);

            if (!m.isRead() && m.getRecipient().getId().equals(me.getId())) {
                unread.put(otherId, unread.getOrDefault(otherId, 0L) + 1);
            }
        }

        return last.entrySet().stream().map(e -> {
            User u = userRepository.findById(e.getKey()).orElse(null);
            ChatMessage msg = e.getValue();

            ChatContactResponse r = new ChatContactResponse();
            r.setUserId(e.getKey());
            r.setName(u != null ? u.getFullname() : "Unknown");
            r.setAvatar(u != null ? u.getAvatarUrl() : null);
            r.setLastMessage(msg.getContent());
            r.setLastTime(msg.getTimestamp().toString());
            r.setUnreadCount(unread.getOrDefault(e.getKey(), 0L));

            return r;
        }).toList();
    }

    @Override
    public List<Long> getChatUserList(String email) {

        User me = getCurrentUser();

        List<ChatMessage> messages = chatMessageRepository.findAllUserMessages(me.getId());

        Set<Long> ids = new LinkedHashSet<>();

        for (ChatMessage m : messages) {
            if (m.getSender().getId().equals(me.getId())) {
                ids.add(m.getRecipient().getId());
            } else {
                ids.add(m.getSender().getId());
            }
        }

        return new ArrayList<>(ids);
    }

    // =====================================================
    // ================= MAPPERS ===========================
    // =====================================================

    private ChatResponse mapToChatResponse(ChatMessage m) {
        return ChatResponse.builder()
                .id(m.getId())
                .senderId(m.getSender().getId())
                .recipientId(m.getRecipient().getId())
                .content(m.getContent())
                .mediaUrl(m.getMediaUrl())
                .fileType(m.getType() != null ? m.getType().name() : null)
                .timestamp(m.getTimestamp())
                .read(m.isRead())
                .messageType(m.getType().name())
                .bookingId(null)
                .build();
    }

    private BookingResponse mapToResponse(BookingClass b) {

        return BookingResponse.builder()
                .bookingId(b.getId())
                .userId(b.getUser().getId())
                .studentName(b.getUser().getFullname())
                .studentEmail(b.getUser().getEmail())
                .status(b.getStatus())
                .createdAt(b.getCreatedAt())
                .build();
    }

    // =====================================================
    // ================= SYSTEM CHAT =======================
    // =====================================================

    private void sendSystemChat(User sender, User recipient, String content) {

        ChatMessage msg = ChatMessage.builder()
                .sender(sender)
                .recipient(recipient)
                .content(content)
                .type(MessageType.SYSTEM)
                .read(false)
                .timestamp(Instant.now())
                .build();

        chatMessageRepository.save(msg);
    }
}