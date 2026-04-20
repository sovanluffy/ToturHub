package com.rental_api.ServiceBooking;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;

import java.net.InetAddress;
import java.net.UnknownHostException;

@SpringBootApplication
public class ServiceBookingApplication {

    private static final Logger logger = LoggerFactory.getLogger(ServiceBookingApplication.class);

    public static void main(String[] args) {
        var context = SpringApplication.run(ServiceBookingApplication.class, args);
        Environment env = context.getEnvironment();

        String protocol = "http";

        // If SSL is enabled, switch to https
        if (env.getProperty("server.ssl.key-store") != null) {
            protocol = "https";
        }

        // Safe default port
        String serverPort = env.getProperty("server.port", "8080");

        // Optional context path
        String contextPath = env.getProperty("server.servlet.context-path", "");

        String hostAddress = "localhost";

        try {
            hostAddress = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            logger.warn("Host name could not be determined, using 'localhost' as fallback");
        }

        String baseLocalUrl = protocol + "://localhost:" + serverPort + contextPath;
        String baseExternalUrl = "https://toturhub-dev.onrender.com" + contextPath;

        logger.info("\n----------------------------------------------------------\n\t" +
                        "Application '{}' is running!\n\t" +
                        "Local: \t\t{}\n\t" +
                        "External: \t{}\n\t" +
                        "Swagger Local: \t{}/swagger-ui/index.html\n\t" +
                                "Swagger Prod: \thttps://toturhub-dev.onrender.com/swagger-ui/index.html\n" +

                        "----------------------------------------------------------",
                env.getProperty("spring.application.name"),
                baseLocalUrl,
                baseExternalUrl,
                baseLocalUrl,
                baseExternalUrl
        );
    }
}