package com.medj.view.inView;

import com.medj.entities.Role;
import lombok.Data;

@Data
public class ApplicationUserInView {
    private String username;
    private String password;
    private Role role;
}
