package com.rental_api.ServiceBooking.Repository;

import com.rental_api.ServiceBooking.Entity.BookingClass;
import com.rental_api.ServiceBooking.Entity.Tutor;
import com.rental_api.ServiceBooking.Entity.User;
import com.rental_api.ServiceBooking.Entity.Enum.BookingStatus;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<BookingClass, Long> {

    // =====================================================
    // ================= USER ==============================
    // =====================================================

    List<BookingClass> findByUser_Id(Long userId);

    long countByUser_Id(Long userId);

    List<BookingClass> findByUser_IdAndStatus(Long userId, BookingStatus status);

    // =====================================================
    // ================= CLASS =============================
    // =====================================================

    List<BookingClass> findByOpenClass_Id(Long classId);

    long countByOpenClass_Id(Long classId);

    // =====================================================
    // ================= TUTOR =============================
    // =====================================================

    List<BookingClass> findByTutor_Id(Long tutorId);

    long countByTutor_Id(Long tutorId);

    long countByTutor_IdAndStatus(Long tutorId, BookingStatus status);

    List<BookingClass> findByTutor_IdAndStatus(Long tutorId, BookingStatus status);

    // =====================================================
    // ================= STATUS ============================
    // =====================================================

    List<BookingClass> findByStatus(BookingStatus status);

    // =====================================================
    // ================= DUPLICATE CHECK ===================
    // =====================================================

    boolean existsByUser_IdAndSchedule_IdAndStatusIn(
            Long userId,
            Long scheduleId,
            List<BookingStatus> statuses
    );

    // ✅ FIXED CLEAN DUPLICATE CHECK (USE THIS IN SERVICE)
    @Query("""
        SELECT CASE WHEN COUNT(b) > 0 THEN true ELSE false END
        FROM BookingClass b
        WHERE b.user = :user
        AND b.tutor = :tutor
        AND b.status = :status
    """)
    boolean existsDuplicateBooking(
            @Param("user") User user,
            @Param("tutor") Tutor tutor,
            @Param("status") BookingStatus status
    );

    // =====================================================
    // ================= JPQL BASIC ========================
    // =====================================================

    @Query("""
        SELECT b FROM BookingClass b
        WHERE b.user.id = :userId
    """)
    List<BookingClass> findUserBookings(@Param("userId") Long userId);

    @Query("""
        SELECT b FROM BookingClass b
        WHERE b.tutor.id = :tutorId
    """)
    List<BookingClass> findTutorBookings(@Param("tutorId") Long tutorId);

    // =====================================================
    // ================= FETCH DETAILS ======================
    // =====================================================

    @Query("""
        SELECT b FROM BookingClass b
        JOIN FETCH b.user
        JOIN FETCH b.openClass
        JOIN FETCH b.schedule
        WHERE b.id = :id
    """)
    Optional<BookingClass> findByIdWithDetails(@Param("id") Long id);

    @Query("""
        SELECT b FROM BookingClass b
        JOIN FETCH b.user
        JOIN FETCH b.openClass
        JOIN FETCH b.schedule
        WHERE b.user.id = :userId
    """)
    List<BookingClass> findAllByUserWithDetails(@Param("userId") Long userId);

    @Query("""
        SELECT b FROM BookingClass b
        JOIN FETCH b.user
        JOIN FETCH b.openClass
        JOIN FETCH b.schedule
        WHERE b.tutor.id = :tutorId
    """)
    List<BookingClass> findAllByTutorWithDetails(@Param("tutorId") Long tutorId);

    @Query("""
        SELECT b FROM BookingClass b
        JOIN FETCH b.user
        JOIN FETCH b.openClass
        JOIN FETCH b.schedule
    """)
    List<BookingClass> findAllWithDetails();

    // =====================================================
    // ================= CONFIRMED STATS ===================
    // =====================================================

    @Query("""
        SELECT COUNT(b)
        FROM BookingClass b
        WHERE b.openClass.id = :classId
        AND b.status = com.rental_api.ServiceBooking.Entity.Enum.BookingStatus.CONFIRMED
    """)
    long countConfirmedByClassId(@Param("classId") Long classId);

    @Query("""
        SELECT b FROM BookingClass b
        JOIN FETCH b.user
        WHERE b.openClass.id = :classId
        AND b.status = com.rental_api.ServiceBooking.Entity.Enum.BookingStatus.CONFIRMED
    """)
    List<BookingClass> findConfirmedStudentsByClassId(@Param("classId") Long classId);

    boolean existsByUser_IdAndTutor_IdAndStatus(
        Long userId,
        Long tutorId,
        BookingStatus status
);
}