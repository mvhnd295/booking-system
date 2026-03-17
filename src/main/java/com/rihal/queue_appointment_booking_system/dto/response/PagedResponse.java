package com.rihal.queue_appointment_booking_system.dto.response;

import org.springframework.data.domain.Page;

import java.util.List;

public record PagedResponse<T>(List<T> results, long total) {
    public static <T> PagedResponse<T> from(Page<?> page, List<T> mappedResults) {
        return new PagedResponse<>(mappedResults, page.getTotalElements());
    }
}
