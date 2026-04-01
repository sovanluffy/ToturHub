    package com.rental_api.ServiceBooking.Entity;

    import java.time.LocalDate;
    import java.time.LocalTime;
    import java.util.List;

    import jakarta.persistence.CascadeType;
    import jakarta.persistence.Entity;
    import jakarta.persistence.GeneratedValue;
    import jakarta.persistence.GenerationType;
    import jakarta.persistence.Id;
    import jakarta.persistence.JoinColumn;
    import jakarta.persistence.ManyToOne;
    import jakarta.persistence.OneToMany;
    import lombok.Data;

    @Entity
    @Data
    public class ScheduleConfig {
        @Id 
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        private String scheduleType; // "WEEKEND", "DAILY"
        private LocalDate startDate;
        private LocalDate endDate;
        private LocalTime startTime;
        private LocalTime endTime;

        @ManyToOne
        @JoinColumn(name = "open_class_id")
        private OpenClass openClass;

    @OneToMany(mappedBy = "config", cascade = CascadeType.ALL)
        private List<ClassSchedule> individualSlots;
    }
