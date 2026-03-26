package com.rental_api.ServiceBooking.Repository;

import com.rental_api.ServiceBooking.Entity.OpenClass;
import com.rental_api.ServiceBooking.Entity.OpenClass.ClassStatus;
import com.rental_api.ServiceBooking.Entity.Subject;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class OpenClassSpecification {

    public static Specification<OpenClass> getFilteredClasses(
            String city, 
            Long subjectId, 
            BigDecimal maxPrice, 
            Integer minExperience) {
        
        return (Root<OpenClass> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 1. Mandatory: Only show OPEN classes from PUBLIC tutors
            predicates.add(cb.equal(root.get("status"), ClassStatus.OPEN));
            predicates.add(cb.isTrue(root.get("tutor").get("isPublic")));

            // 2. Filter by City (Location)
            if (city != null && !city.isEmpty()) {
                predicates.add(cb.equal(root.get("city"), city));
            }

            // 3. Filter by Subject
            if (subjectId != null) {
                Join<OpenClass, Subject> subjectJoin = root.join("subjects");
                predicates.add(cb.equal(subjectJoin.get("id"), subjectId));
            }

            // 4. Filter by Max Price
            if (maxPrice != null) {
                // Join the Map table 'class_pricing'
                MapJoin<OpenClass, Integer, BigDecimal> priceJoin = root.joinMap("priceOptions");
                predicates.add(cb.lessThanOrEqualTo(priceJoin.value(), maxPrice));
            }

            // 5. Filter by Tutor Experience
            if (minExperience != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("tutor").get("yearsOfExperience"), minExperience));
            }

            // Avoid duplicate results when joining lists
            query.distinct(true);

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}