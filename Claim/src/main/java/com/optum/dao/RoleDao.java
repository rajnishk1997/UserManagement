package com.optum.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.optum.entity.Role;

@Repository
public interface RoleDao extends JpaRepository<Role, String> {

}
