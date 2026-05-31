package com.medj.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Data;


@Entity
@Table(name = "ApplicationUser")
@Data
public class ApplicationUser extends BaseEntity{

    private String username;
    private String password;
    private String firstName;
    private String lastName;
    private String phone;
    private String address;
    @Enumerated(EnumType.STRING)
    private Role role;
}
