package com.medj.view.inView;

import jakarta.annotation.Nullable;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class AppointmentInView {
    private String name;
    private String place;
    private LocalDate date;
    @Nullable
    private Long practitionerId;
    private List<Long> documents;
}
