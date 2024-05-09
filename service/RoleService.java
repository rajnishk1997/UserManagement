package com.optum.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.optum.dao.RoleDao;
import com.optum.entity.Role;

@Service
public class RoleService {

    @Autowired
    private RoleDao roleDao;

    public Role createNewRole(Role role) {
    	// Extract roleName from the incoming Role object
        String roleName = role.getRoleName();

        // Set the roleName to the incoming Role object (optional)
        role.setRoleName(roleName);

        return roleDao.save(role);
    }
}
