package com.rental_api.ServiceBooking.Repository;

import com.rental_api.ServiceBooking.Entity.ScheduleConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * Repository interface for ScheduleConfig entity.
 * Provides standard CRUD operations and custom query methods.
 */
@Repository
public interface ScheduleConfigRepository extends JpaRepository<ScheduleConfig, Long> {

    /**
     * Optional: Find all schedule configurations for a specific class.
     * Useful if you want to show a student all available time slots for a class.
     */
    List<ScheduleConfig> findByOpenClassId(Long openClassId);

}