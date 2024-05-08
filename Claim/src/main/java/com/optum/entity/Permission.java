package com.optum.entity;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class Permission {

    @Id
    private String permissionName;

    // Constructors
    public Permission() {
    }

    public Permission(String permissionName) {
        this.permissionName = permissionName;
    }

    // Getters and Setters
    public String getPermissionName() {
        return permissionName;
    }

    public void setPermissionName(String permissionName) {
        this.permissionName = permissionName;
    }
}

