package com.rihal.queue_appointment_booking_system.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "staff_service_types")
@IdClass(StaffServiceType.StaffServiceTypeId.class)
@Getter
@Setter
@NoArgsConstructor
public class StaffServiceType {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "staff_id", nullable = false)
    private Staff staff;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_type_id", nullable = false)
    private ServiceType serviceType;

    // ── Compositing the id which consists of the staff's and the serviceType's id ────────────────────────────────────
    public static class StaffServiceTypeId implements Serializable {

        private UUID staff;
        private UUID serviceType;

        public StaffServiceTypeId() {}

        public StaffServiceTypeId(UUID staff, UUID serviceType) {
            this.staff = staff;
            this.serviceType = serviceType;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof StaffServiceTypeId that)) return false;
            return Objects.equals(staff, that.staff) &&
                    Objects.equals(serviceType, that.serviceType);
        }

        @Override
        public int hashCode() {
            return Objects.hash(staff, serviceType);
        }
    }
}
