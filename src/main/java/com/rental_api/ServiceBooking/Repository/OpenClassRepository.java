package com.rental_api.ServiceBooking.Repository;

import com.rental_api.ServiceBooking.Entity.*;
import com.rental_api.ServiceBooking.Entity.OpenClass.ClassStatus;
import com.rental_api.ServiceBooking.Entity.OpenClass.LearningMode;
import com.rental_api.ServiceBooking.Entity.OpenClass.VisibilityStatus;

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

    // =========================================================
    // 🌍 PUBLIC FEED (GLOBAL HOME FEED)
    // =========================================================

    @Query("""
        SELECT c FROM OpenClass c
        WHERE c.status = :status
        AND c.visibilityStatus = :visibilityStatus
    """)
    List<OpenClass> findByStatusAndVisibilityStatus(
            @Param("status") ClassStatus status,
            @Param("visibilityStatus") VisibilityStatus visibilityStatus
    );

    // OPTIONAL safer public feed (if you still want tutor filter)
    @Query("""
        SELECT c FROM OpenClass c
        WHERE c.status = :status
        AND c.visibilityStatus = :visibilityStatus
        AND c.tutor.isPublic = true
    """)
    List<OpenClass> findAllPublicFeed(
            @Param("status") ClassStatus status,
            @Param("visibilityStatus") VisibilityStatus visibilityStatus
    );

    // =========================================================
    // 🎯 LEARNING MODE FILTER
    // =========================================================

    @Query("""
        SELECT c FROM OpenClass c
        JOIN c.learningModes m
        WHERE m = :mode
        AND c.status = :status
    """)
    List<OpenClass> findByLearningMode(
            @Param("mode") LearningMode mode,
            @Param("status") ClassStatus status
    );

    // =========================================================
    // 👤 TUTOR OWNER VIEW
    // =========================================================

    List<OpenClass> findByTutorId(Long tutorId);

    List<OpenClass> findAllByTutorIn(List<Tutor> tutors);

    // =========================================================
    // 👀 PUBLIC CLASSES BY TUTOR (PROFILE PAGE)
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
    // 👥 CONFIRMED STUDENTS LIST
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