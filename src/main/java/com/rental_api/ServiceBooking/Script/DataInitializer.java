package com.rental_api.ServiceBooking.Script;

import com.rental_api.ServiceBooking.Entity.Role;
import com.rental_api.ServiceBooking.Entity.Tutor;
import com.rental_api.ServiceBooking.Entity.User;
import com.rental_api.ServiceBooking.Repository.RoleRepository;
import com.rental_api.ServiceBooking.Repository.TutorRepository;
import com.rental_api.ServiceBooking.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final TutorRepository tutorRepository; // Added
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional // Ensures atomicity for multiple table inserts
    public void run(String... args) throws Exception {
        seedRoles();
        seedDefaultTutor();
    }

    private void seedRoles() {
        String[] roles = {"admin", "tutor", "student"};
        for (String roleName : roles) {
            if (roleRepository.findByName(roleName).isEmpty()) {
                Role role = new Role();
                role.setName(roleName);
                roleRepository.save(role);
                System.out.println("✅ Role created: " + roleName);
            }
        }
    }

    

    private void seedDefaultTutor() {
        String tutorEmail = "tutor@test.com";
        
        if (userRepository.findByEmail(tutorEmail).isEmpty()) {
            Role tutorRole = roleRepository.findByName("tutor").orElseThrow();

            // 1. Create the User Entity
            User tutorUser = User.builder()
                    .fullname("John the Tutor")
                    .username("johntutor")
                    .email(tutorEmail)
                    .password(passwordEncoder.encode("tutor123"))
                    .status(User.Status.ACTIVE)
                    .roles(new HashSet<>(Set.of(tutorRole)))
                    .build();

            userRepository.save(tutorUser);

            // 2. Create the Tutor Profile Entity (Linked to User)
            Tutor tutorProfile = Tutor.builder()
                    .user(tutorUser)
                    .bio("Hi! I am a professional Mathematics tutor with 10 years experience.")
                    .profilePicture("https://api.dicebear.com/7.x/avataaars/svg?seed=John")
                    .averageRating(5.0)
                    .totalStudentsTaught(0)
                    .build();

            tutorRepository.save(tutorProfile);
            System.out.println("✅ Tutor user and profile seeded: " + tutorEmail);
        }
    }
}