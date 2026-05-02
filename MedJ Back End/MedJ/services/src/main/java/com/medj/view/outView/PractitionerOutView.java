package com.medj.view.outView;

import lombok.Data;

@Data
public class PractitionerOutView {

    private Long id;
    private String firstName;
    private String lastName;
    private SpecialtyOutView specialty;
    private String specialization;

}
