package com.rental_api.ServiceBooking.Repository;

import com.rental_api.ServiceBooking.Entity.OpenClass;
import com.rental_api.ServiceBooking.Entity.OpenClass.ClassStatus;
import com.rental_api.ServiceBooking.Entity.OpenClass.LearningMode;
import com.rental_api.ServiceBooking.Entity.Tutor;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OpenClassRepository extends JpaRepository<OpenClass, Long>,
        JpaSpecificationExecutor<OpenClass> {

    // ================= LOCATION FILTER =================
    List<OpenClass> findByLocation_CityAndStatus(String city, ClassStatus status);

    List<OpenClass> findByLocation_DistrictAndStatus(String district, ClassStatus status);

    // ================= LEARNING MODE =================
    List<OpenClass> findByLearningModesContainingAndStatus(LearningMode mode, ClassStatus status);

    // ================= TUTOR =================
    List<OpenClass> findByTutorId(Long tutorId);

    List<OpenClass> findAllByTutorIn(List<Tutor> tutors);

    // =========================================================
    // 🔥 NEW: CONFIRMED STUDENT COUNT (BEST PERFORMANCE)
    // =========================================================

    @Query("""
        SELECT COUNT(b)
        FROM BookingClass b
        WHERE b.openClass.id = :classId
        AND b.status = com.rental_api.ServiceBooking.Entity.Enum.BookingStatus.CONFIRMED
    """)
    long countConfirmedStudentsByClassId(@Param("classId") Long classId);

    // =========================================================
    // 🔥 NEW: CONFIRMED STUDENTS (FOR PROFILE PAGE)
    // =========================================================

    @Query("""
        SELECT b
        FROM BookingClass b
        JOIN FETCH b.user
        WHERE b.openClass.id = :classId
        AND b.status = com.rental_api.ServiceBooking.Entity.Enum.BookingStatus.CONFIRMED
    """)
    List<com.rental_api.ServiceBooking.Entity.BookingClass> findConfirmedStudentsByClassId(
            @Param("classId") Long classId
    );
}