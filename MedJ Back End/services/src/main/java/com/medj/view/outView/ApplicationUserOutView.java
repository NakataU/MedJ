package com.medj.view.outView;

import com.medj.entities.Role;
import lombok.Data;

@Data
public class ApplicationUserOutView {
    private String username;
    private String password;
    private Role role;
    private String firstName;
    private String lastName;
    private String phone;
    private String address;
}
