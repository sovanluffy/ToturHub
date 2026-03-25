package com.rental_api.ServiceBooking.Script;

import com.rental_api.ServiceBooking.Entity.Role;
import com.rental_api.ServiceBooking.Entity.User;
import com.rental_api.ServiceBooking.Repository.RoleRepository;
import com.rental_api.ServiceBooking.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${ADMIN_FULLNAME}")
    private String adminFullname;

    @Value("${ADMIN_EMAIL}")
    private String adminEmail;

    @Value("${ADMIN_PASSWORD}")
    private String adminPassword;

    @Value("${ADMIN_PHONE}")
    private String adminPhone;

    @Value("${ADMIN_ADDRESS}")
    private String adminAddress;

    @Value("${ADMIN_LOCATION}")
    private String adminLocation;

    @Override
    public void run(String... args) throws Exception {
        // Check if admin already exists
        if (userRepository.findByEmail(adminEmail).isEmpty()) {

            // Get or create admin role
            Role adminRole = roleRepository.findByName("admin")
                    .orElseGet(() -> {
                        Role role = new Role();
                        role.setName("admin");
                        return roleRepository.save(role);
                    });

            // Generate username from email (everything before @)
            String username = adminEmail.contains("@") ? adminEmail.substring(0, adminEmail.indexOf("@")) : "admin";

            // Create admin user
            User admin = User.builder()
                    .fullname(adminFullname)
                    .username(username) // <-- fixed: ensure NOT NULL
                    .email(adminEmail)
                    .password(passwordEncoder.encode(adminPassword))
                    .phone(adminPhone)
                    .address(adminAddress)
                    .location(adminLocation)
                    .status(User.Status.ACTIVE) // enum ACTIVE
                    .roles(Set.of(adminRole))
                    .build();

            userRepository.save(admin);
            System.out.println("✅ Admin user seeded successfully: " + adminEmail);
        } else {
            System.out.println("⚠️ Admin user already exists: " + adminEmail);
        }
    }
}