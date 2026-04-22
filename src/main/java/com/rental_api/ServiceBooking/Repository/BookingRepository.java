package com.rental_api.ServiceBooking.Repository;

import com.rental_api.ServiceBooking.Entity.BookingClass;
import com.rental_api.ServiceBooking.Entity.Enum.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<BookingClass, Long> {

    // ================= USER =================

    List<BookingClass> findByUser_Id(Long userId);

    boolean existsByUser_IdAndSchedule_Id(Long userId, Long scheduleId);

    long countByUser_Id(Long userId);


    // ================= CLASS =================

    List<BookingClass> findByOpenClass_Id(Long classId);


    // ================= TUTOR =================

    List<BookingClass> findByTutor_Id(Long tutorId);

    long countByTutor_Id(Long tutorId);

    long countByTutor_IdAndStatus(Long tutorId, BookingStatus status);


    // ================= STATUS FILTER =================

    List<BookingClass> findByStatus(BookingStatus status);

    List<BookingClass> findByTutor_IdAndStatus(Long tutorId, BookingStatus status);

    List<BookingClass> findByUser_IdAndStatus(Long userId, BookingStatus status);


    // ================= ⭐ RULE CHECK (IMPORTANT FIX) =================

    boolean existsByUser_IdAndSchedule_IdAndStatusIn(
            Long userId,
            Long scheduleId,
            List<BookingStatus> statuses
    );


    // ================= JPQL QUERIES =================

    @Query("""
        SELECT b FROM BookingClass b
        WHERE b.tutor.id = :tutorId
    """)
    List<BookingClass> findTutorBookings(@Param("tutorId") Long tutorId);


    @Query("""
        SELECT b FROM BookingClass b
        WHERE b.user.id = :userId
    """)
    List<BookingClass> findUserBookings(@Param("userId") Long userId);


    // ================= 🔥 JOIN FETCH (FIX LAZY LOADING) =================

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
    """)
    List<BookingClass> findAllWithDetails();


    @Query("""
        SELECT b FROM BookingClass b
        JOIN FETCH b.user
        JOIN FETCH b.openClass
        JOIN FETCH b.schedule
        WHERE b.user.id = :userId
    """)
    List<BookingClass> findAllByUserWithDetails(@Param("userId") Long userId);
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
    @Query("""
        SELECT b FROM BookingClass b
        JOIN FETCH b.user
        JOIN FETCH b.openClass
        JOIN FETCH b.schedule
        WHERE b.tutor.id = :tutorId
    """)
    List<BookingClass> findAllByTutorWithDetails(@Param("tutorId") Long tutorId);
}