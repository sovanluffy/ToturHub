package com.rental_api.ServiceBooking.Repository;

import com.rental_api.ServiceBooking.Entity.Role;
import com.rental_api.ServiceBooking.Entity.User;
import com.rental_api.ServiceBooking.Entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, Long> {
    // Add this line to fix the "cannot find symbol" error
    List<UserRole> findByUser(User user);
        boolean existsByUserAndRole(User user, Role role);
        
}