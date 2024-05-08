package com.optum.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.optum.entity.*;

public interface PermissionDao extends JpaRepository<Permission, String> {

	Permission findByPermissionName(String string);
    // You can define custom query methods here if needed
}
