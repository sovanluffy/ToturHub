package com.rental_api.ServiceBooking.Entity;


import jakarta.persistence.Table;
import lombok.Data;

@Data
@Table(name = "notifications")
public class Notification {
    private String content;
    private String timestamp;
}
