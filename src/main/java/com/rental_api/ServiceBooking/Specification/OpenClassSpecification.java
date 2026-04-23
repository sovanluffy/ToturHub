package com.rental_api.ServiceBooking.Specification;

import com.rental_api.ServiceBooking.Entity.OpenClass;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class OpenClassSpecification {

    public static Specification<OpenClass> filter(String location, String subject) {
        return (root, query, cb) -> {

            query.distinct(true);

            List<Predicate> predicates = new ArrayList<>();

            if (location != null && !location.trim().isEmpty()) {
                Join<Object, Object> locJoin = root.join("location", JoinType.LEFT);

                Predicate district = cb.like(
                        cb.lower(locJoin.get("district")),
                        "%" + location.toLowerCase() + "%"
                );

                Predicate city = cb.like(
                        cb.lower(locJoin.get("city")),
                        "%" + location.toLowerCase() + "%"
                );

                predicates.add(cb.or(district, city));
            }

            if (subject != null && !subject.trim().isEmpty()) {
                Join<Object, Object> subjectJoin = root.join("subjects", JoinType.LEFT);

                predicates.add(
                        cb.like(
                                cb.lower(subjectJoin.get("name")),
                                "%" + subject.toLowerCase() + "%"
                        )
                );
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
