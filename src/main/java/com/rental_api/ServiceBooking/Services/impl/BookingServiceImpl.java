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

    /* ================= UTIL HELPERS ================= */
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

    /* ================= BOOKING CORE LOGIC ================= */
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

        sendAutoSystemChat(student, openClass.getTutor().getUser(),
                "📌 New booking request from " + student.getFullname() + " for " + openClass.getTitle());

        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public BookingResponse confirmBooking(Long bookingId) {
        BookingClass booking = bookingRepository.findByIdWithDetails(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));
        
        booking.setStatus(BookingStatus.CONFIRMED);
        BookingClass saved = bookingRepository.save(booking);

        sendAutoSystemChat(
            saved.getTutor().getUser(), 
            saved.getUser(), 
            "✅ Your booking for '" + saved.getOpenClass().getTitle() + "' has been CONFIRMED!"
        );

        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public BookingResponse rejectBooking(Long bookingId) {
        BookingClass booking = bookingRepository.findByIdWithDetails(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));
        
        booking.setStatus(BookingStatus.REJECTED);
        BookingClass saved = bookingRepository.save(booking);

        sendAutoSystemChat(
            saved.getTutor().getUser(), 
            saved.getUser(), 
            "❌ Sorry, your booking for '" + saved.getOpenClass().getTitle() + "' has been REJECTED."
        );

        return mapToResponse(saved);
    }

    /* ================= REAL-TIME CHAT SERVICE ================= */
    private void sendAutoSystemChat(User sender, User recipient, String content) {
        ChatMessage msg = ChatMessage.builder()
                .sender(sender)
                .recipient(recipient)
                .content(content)
                .type(MessageType.SYSTEM)
                .read(false)
                .timestamp(Instant.now())
                .build();

        ChatMessage saved = chatMessageRepository.save(msg);
        ChatResponse response = mapToChatResponse(saved);
        
        messagingTemplate.convertAndSendToUser(
                recipient.getEmail(),
                "/queue/messages",
                response
        );
    }

    @Override
    @Transactional
    public ChatResponse sendMessage(String senderEmail, ChatRequest request) {
        User sender = getCurrentUser();
        User recipient = userRepository.findById(request.getRecipientId())
                .orElseThrow(() -> new ResourceNotFoundException("Recipient not found"));

        String mediaUrl = null;
        String messageTypeStr = "USER";

        if (request.getFile() != null && !request.getFile().isEmpty()) {
            try {
                String contentType = request.getFile().getContentType();
                Map upload = cloudinary.uploader().upload(
                        request.getFile().getBytes(),
                        ObjectUtils.asMap("folder", "tutorhub_chat", "resource_type", "auto")
                );
                mediaUrl = upload.get("secure_url").toString();

                if (contentType != null) {
                    if (contentType.startsWith("image")) messageTypeStr = "IMAGE";
                    else if (contentType.startsWith("audio")) messageTypeStr = "AUDIO";
                    else if (contentType.startsWith("video")) messageTypeStr = "VIDEO";
                }
            } catch (IOException e) {
                throw new RuntimeException("Media upload failed");
            }
        }

        ChatMessage message = ChatMessage.builder()
                .sender(sender)
                .recipient(recipient)
                .content(request.getContent())
                .type(MessageType.valueOf(messageTypeStr))
                .mediaUrl(mediaUrl)
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

    /* ================= QUERIES & DATA RETRIEVAL ================= */
    @Override
    public List<ChatResponse> getChatHistory(String myEmail, Long otherUserId) {
        User me = getCurrentUser();
        return chatMessageRepository.findChatHistory(me.getId(), otherUserId)
                .stream().map(this::mapToChatResponse).toList();
    }

    @Override
    @Transactional
    public void markMessagesAsRead(String recipientEmail, Long senderId) {
        User me = getCurrentUser();
        List<ChatMessage> unread = chatMessageRepository.findByRecipient_IdAndSender_IdAndReadFalse(me.getId(), senderId);
        unread.forEach(m -> m.setRead(true));
        chatMessageRepository.saveAll(unread);
    }

    @Override
    public List<ChatContactResponse> getChatContacts(String email) {
        User me = getCurrentUser();
        List<ChatMessage> messages = chatMessageRepository.findAllUserMessages(me.getId());

        Map<Long, ChatMessage> last = new LinkedHashMap<>();
        Map<Long, Long> unreadCountMap = new HashMap<>();

        for (ChatMessage m : messages) {
            Long otherId = m.getSender().getId().equals(me.getId()) ? m.getRecipient().getId() : m.getSender().getId();
            last.putIfAbsent(otherId, m);
            if (!m.isRead() && m.getRecipient().getId().equals(me.getId())) {
                unreadCountMap.put(otherId, unreadCountMap.getOrDefault(otherId, 0L) + 1);
            }
        }

        return last.entrySet().stream().map(e -> {
            User u = userRepository.findById(e.getKey()).orElse(null);
            return ChatContactResponse.builder()
                    .userId(e.getKey())
                    .name(u != null ? u.getFullname() : "Unknown")
                    .avatar(u != null ? u.getAvatarUrl() : null)
                    .lastMessage(e.getValue().getContent())
                    .lastTime(e.getValue().getTimestamp().toString())
                    .unreadCount(unreadCountMap.getOrDefault(e.getKey(), 0L))
                    .build();
        }).toList();
    }

    /* ================= MAPPERS ================= */
    private ChatResponse mapToChatResponse(ChatMessage m) {
        return ChatResponse.builder()
                .id(m.getId())
                .senderId(m.getSender().getId())
                .recipientId(m.getRecipient().getId())
                .senderName(m.getSender().getFullname())
                .senderAvatar(m.getSender().getAvatarUrl())
                .content(m.getContent())
                .mediaUrl(m.getMediaUrl())
                .messageType(m.getType() != null ? m.getType().name() : "USER")
                .timestamp(m.getTimestamp())
                .read(m.isRead())
                .build();
    }

    // FIXED MAPPER - Now includes classId and scheduleId
    private BookingResponse mapToResponse(BookingClass b) {
        return BookingResponse.builder()
                .bookingId(b.getId())
                .userId(b.getUser().getId())
                .studentName(b.getUser().getFullname())
                .studentEmail(b.getUser().getEmail())
                .studentPhone(b.getUser().getPhone())
                .studentAvatar(b.getUser().getAvatarUrl())
                
                // IMPORTANT FIX
                .classId(b.getOpenClass() != null ? b.getOpenClass().getId() : null)
                .scheduleId(b.getSchedule() != null ? b.getSchedule().getId() : null)
                
                .classTitle(b.getOpenClass() != null ? b.getOpenClass().getTitle() : null)
                .day(b.getSchedule() != null ? b.getSchedule().getDay() : null)
                .startTime(b.getSchedule() != null ? b.getSchedule().getStartTime() : null)
                .endTime(b.getSchedule() != null ? b.getSchedule().getEndTime() : null)
                .status(b.getStatus())
                .note(b.getNote())
                .telegram(b.getTelegram())
                .createdAt(b.getCreatedAt())
                .build();
    }

    /* ================= COUNTS & SEARCHES ================= */
    @Override public Long getMyPendingBookingsCount() { 
        return bookingRepository.countByTutor_IdAndStatus(getTutor(getCurrentUser()).getId(), BookingStatus.PENDING); 
    }
    
    @Override public Long getUnreadMessageCount(String email) { 
        return chatMessageRepository.countUnreadByRecipient(getCurrentUser().getId()); 
    }
    
    @Override public List<Long> getChatUserList(String email) { 
        User me = getCurrentUser();
        return chatMessageRepository.findAllUserMessages(me.getId()).stream()
                .map(m -> m.getSender().getId().equals(me.getId()) ? m.getRecipient().getId() : m.getSender().getId())
                .distinct().toList();
    }
    
    @Override public List<BookingResponse> getBookingsByUserId(Long userId) { 
        return bookingRepository.findAllByUserWithDetails(userId).stream().map(this::mapToResponse).toList(); 
    }
    
    @Override public List<BookingResponse> getBookingsByClassId(Long classId) { 
        return bookingRepository.findByOpenClass_Id(classId).stream().map(this::mapToResponse).toList(); 
    }
    
    @Override public List<BookingResponse> getBookingsByTutorId(Long tutorId) { 
        return bookingRepository.findAllByTutorWithDetails(tutorId).stream().map(this::mapToResponse).toList(); 
    }
    
    @Override public List<BookingResponse> getMyBookings() { 
        return bookingRepository.findAllByUserWithDetails(getCurrentUser().getId()).stream().map(this::mapToResponse).toList(); 
    }
    
    @Override public List<BookingResponse> getMyTutorBookings() { 
        return bookingRepository.findAllByTutorWithDetails(getTutor(getCurrentUser()).getId()).stream().map(this::mapToResponse).toList(); 
    }
}