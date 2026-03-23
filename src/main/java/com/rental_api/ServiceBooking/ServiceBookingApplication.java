package com.rental_api.ServiceBooking;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SpringBootApplication
public class ServiceBookingApplication {

    private static final Logger logger = LoggerFactory.getLogger(ServiceBookingApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(ServiceBookingApplication.class, args);

        logger.info("ServiceBookingApplication started successfully!");
        logger.info("🎉 Swagger UI is ready! Open it in your browser: http://localhost:8080/swagger-ui/index.html");
    }
}
