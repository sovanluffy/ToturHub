package com.rental_api.ServiceBooking.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.rental_api.ServiceBooking.Entity.Review;
import com.rental_api.ServiceBooking.Entity.Tutor;
import com.rental_api.ServiceBooking.Entity.User;

public interface ReviewRepostory extends JpaRepository<Review, Long> {

    boolean existsByStudentAndTutor(User currentUser, Tutor tutor);

}
