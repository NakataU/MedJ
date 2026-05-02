package com.medj.entities;

public enum Role {
    ADMIN,
    REGULAR;

    public String addPrefix(Role role){
        return "ROLE_" + role;
    }
}
