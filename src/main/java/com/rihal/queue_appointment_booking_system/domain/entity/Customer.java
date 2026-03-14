package com.rihal.queue_appointment_booking_system.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "customers")
@PrimaryKeyJoinColumn(name = "id")
@Getter
@Setter
@NoArgsConstructor
public class Customer extends User {

    @Column(name = "phone", length = 20)
    private String phone;

    // Stored as a file path reference — never expose directly in API responses
    // Served only through secured /api/files/{id} endpoint
    @Column(name = "id_image_path", length = 500)
    private String idImagePath;

    @Column(name = "id_image_size")
    private Long idImageSize;

    @Column(name = "id_image_type", length = 50)
    private String idImageType;
}
