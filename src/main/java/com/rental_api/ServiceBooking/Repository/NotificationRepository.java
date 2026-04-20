package com.rental_api.ServiceBooking.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.rental_api.ServiceBooking.Entity.Notification;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    Long countByRecipientEmailAndIsReadFalse(String name);
    // No additional methods needed for basic CRUD

    List<Notification> findAllByRecipientEmailAndIsReadFalse(String name);

}
