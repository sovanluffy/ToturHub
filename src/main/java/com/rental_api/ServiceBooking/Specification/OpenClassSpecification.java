package com.rental_api.ServiceBooking.Specification;

import com.rental_api.ServiceBooking.Entity.OpenClass;
import com.rental_api.ServiceBooking.Entity.Subject;
import com.rental_api.ServiceBooking.Entity.Tutor;
import com.rental_api.ServiceBooking.Entity.Location;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.MapJoin;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class OpenClassSpecification {

    public static Specification<OpenClass> getFilteredClasses(
            String city, 
            String district, 
            Long subjectId, 
            BigDecimal maxPrice, 
            Integer minExp) {
        
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 1. Filter by City (Joins the Location table)
            if (city != null && !city.isEmpty()) {
                predicates.add(cb.equal(root.get("location").get("city"), city));
            }

            // 2. Filter by District
            if (district != null && !district.isEmpty()) {
                predicates.add(cb.equal(root.get("location").get("district"), district));
            }

            // 3. Filter by Subject ID (Many-to-Many Join)
            if (subjectId != null) {
                Join<OpenClass, Subject> subjectJoin = root.join("subjects", JoinType.INNER);
                predicates.add(cb.equal(subjectJoin.get("id"), subjectId));
            }

            // 4. Filter by Max Price (Handles the Map Collection)
            if (maxPrice != null) {
                // Use joinMap specifically to access .value() for the Price amount
                MapJoin<OpenClass, Integer, BigDecimal> pricingJoin = root.joinMap("priceOptions", JoinType.INNER);
                predicates.add(cb.le(pricingJoin.value(), maxPrice));
            }

            // 5. Filter by Tutor Experience
            if (minExp != null) {
                Join<OpenClass, Tutor> tutorJoin = root.join("tutor", JoinType.INNER);
                predicates.add(cb.ge(tutorJoin.get("yearsOfExperience"), minExp));
            }

            // Ensure we don't return duplicate classes if they match multiple criteria
            query.distinct(true);

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}