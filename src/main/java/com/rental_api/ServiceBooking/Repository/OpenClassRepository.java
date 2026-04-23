package com.rental_api.ServiceBooking.Repository;

import com.rental_api.ServiceBooking.Entity.BookingClass;
import com.rental_api.ServiceBooking.Entity.OpenClass;
import com.rental_api.ServiceBooking.Entity.OpenClass.ClassStatus;
import com.rental_api.ServiceBooking.Entity.OpenClass.LearningMode;
import com.rental_api.ServiceBooking.Entity.OpenClass.VisibilityStatus;
import com.rental_api.ServiceBooking.Entity.Tutor;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OpenClassRepository extends JpaRepository<OpenClass, Long>,
        JpaSpecificationExecutor<OpenClass> {

    // =========================================================
    // 📍 LOCATION FILTER
    // =========================================================

    List<OpenClass> findByLocation_CityAndStatus(
            String city,
            ClassStatus status
    );

    List<OpenClass> findByLocation_DistrictAndStatus(
            String district,
            ClassStatus status
    );

    List<OpenClass> findByLocation_CityAndStatusAndVisibilityStatus(
            String city,
            ClassStatus status,
            VisibilityStatus visibilityStatus
    );

    // =========================================================
    // 🎯 LEARNING MODE FILTER
    // =========================================================

    List<OpenClass> findByLearningModesContainingAndStatus(
            LearningMode mode,
            ClassStatus status
    );

    // =========================================================
    // 👤 TUTOR (OWNER VIEW - ALL CLASSES)
    // =========================================================

    List<OpenClass> findByTutorId(Long tutorId);

    List<OpenClass> findAllByTutorIn(List<Tutor> tutors);

    // =========================================================
    // 🌍 PUBLIC CLASSES (GLOBAL)
    // =========================================================

    List<OpenClass> findByStatusAndVisibilityStatus(
            ClassStatus status,
            VisibilityStatus visibilityStatus
    );

    // =========================================================
    // 🌍 PUBLIC CLASSES BY TUTOR (PROFILE PAGE FIXED)
    // =========================================================

    @Query("""
        SELECT c FROM OpenClass c
        WHERE c.tutor.id = :tutorId
        AND c.status = :status
        AND c.visibilityStatus = :visibilityStatus
    """)
    List<OpenClass> findPublicClassesByTutorId(
            @Param("tutorId") Long tutorId,
            @Param("status") ClassStatus status,
            @Param("visibilityStatus") VisibilityStatus visibilityStatus
    );

    // =========================================================
    // 📊 CONFIRMED STUDENT COUNT
    // =========================================================

    @Query("""
        SELECT COUNT(b)
        FROM BookingClass b
        WHERE b.openClass.id = :classId
        AND b.status = com.rental_api.ServiceBooking.Entity.Enum.BookingStatus.CONFIRMED
    """)
    long countConfirmedStudentsByClassId(@Param("classId") Long classId);

    // =========================================================
    // 👥 CONFIRMED STUDENTS (NO N+1)
    // =========================================================

    @Query("""
        SELECT b
        FROM BookingClass b
        JOIN FETCH b.user
        WHERE b.openClass.id = :classId
        AND b.status = com.rental_api.ServiceBooking.Entity.Enum.BookingStatus.CONFIRMED
    """)
    List<BookingClass> findConfirmedStudentsByClassId(@Param("classId") Long classId);
}