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
        
        // Try to get the Render URL or fallback to localhost
        String protocol = "http";
        if (env.getProperty("server.ssl.key-store") != null) {
            protocol = "https";
        }
        
        String serverPort = env.getProperty("server.port");
        String contextPath = env.getProperty("server.servlet.context-path", "");
        String hostAddress = "localhost";
        
        try {
            hostAddress = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            logger.warn("The host name could not be determined, using 'localhost' as fallback");
        }

        logger.info("\n----------------------------------------------------------\n\t" +
                "Application '{}' is running!\n\t" +
                "Local: \t\t{}://localhost:{}{}\n\t" +
                "External: \thttps://toturhub-dev.onrender.com{}\n\t" +
                "Swagger UI: \thttps://toturhub-dev.onrender.com/swagger-ui/index.html\n" +
                "----------------------------------------------------------",
                env.getProperty("spring.application.name"),
                protocol,
                serverPort,
                contextPath,
                contextPath);
    }
}