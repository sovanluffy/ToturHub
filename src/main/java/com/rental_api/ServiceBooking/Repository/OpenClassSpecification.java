package com.rental_api.ServiceBooking.Repository;

import com.rental_api.ServiceBooking.Entity.OpenClass;
import com.rental_api.ServiceBooking.Entity.OpenClass.ClassStatus;
import com.rental_api.ServiceBooking.Entity.Subject;
import com.rental_api.ServiceBooking.Entity.Location; // Added
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

            // 1. Logic: Only show OPEN classes
            predicates.add(cb.equal(root.get("status"), ClassStatus.OPEN));
            
            // 2. Filter by City (Fix: Must join with Location table)
            if (city != null && !city.isEmpty()) {
                // We navigate: OpenClass -> Location (entity) -> City (field)
                Join<OpenClass, Location> locationJoin = root.join("location");
                predicates.add(cb.equal(locationJoin.get("city"), city));
            }

            // 3. Filter by Subject
            if (subjectId != null) {
                Join<OpenClass, Subject> subjectJoin = root.join("subjects");
                predicates.add(cb.equal(subjectJoin.get("id"), subjectId));
            }

            // 4. Filter by Max Price (Map Join)
            if (maxPrice != null) {
                MapJoin<OpenClass, Integer, BigDecimal> priceJoin = root.joinMap("priceOptions");
                predicates.add(cb.lessThanOrEqualTo(priceJoin.value(), maxPrice));
            }

            // 5. Filter by Tutor Experience
            if (minExperience != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("tutor").get("yearsOfExperience"), minExperience));
            }

            // Avoid duplicate results (SQL DISTINCT)
            query.distinct(true);

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

}