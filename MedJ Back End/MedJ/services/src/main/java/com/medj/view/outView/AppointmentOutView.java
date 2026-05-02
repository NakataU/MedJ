package com.medj.view.outView;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class AppointmentOutView {

    private Long id;
    private String name;
    private String place;
    private LocalDate date;
    private PractitionerOutView practitioner;
    private List<DocumentListOutView> appointmentDocuments;

}
