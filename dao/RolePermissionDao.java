package com.optum.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import com.optum.entity.RolePermission;

public interface RolePermissionDao extends JpaRepository<RolePermission, Integer>  {

}
