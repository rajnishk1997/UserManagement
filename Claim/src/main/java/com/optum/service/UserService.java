package com.optum.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.optum.dao.PermissionDao;
import com.optum.dao.ReqRes;
import com.optum.dao.RoleDao;
import com.optum.dao.UserDao;
import com.optum.entity.Permission;
import com.optum.entity.Role;
import com.optum.entity.User;

import java.util.Optional;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class UserService {
	
	@Autowired
    private PermissionDao permissionRepository;

    @Autowired
    private UserDao userDao;

    @Autowired
    private RoleDao roleDao;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public void initRoleAndUser() {
    	//Role 1- Admin
        Role adminRole = new Role();
        adminRole.setRoleName("Admin");
        adminRole.setRoleDescription("Admin role");
        roleDao.save(adminRole);

        //Role 2- User
        Role userRole = new Role();
        userRole.setRoleName("User");
        userRole.setRoleDescription("Default role for newly created record");
        roleDao.save(userRole);
        
        //User 1 - Admin User
        User adminUser = new User();
        adminUser.setUserName("admin123");
        adminUser.setUserPassword(getEncodedPassword("admin@pass"));
        adminUser.setUserFirstName("admin");
        adminUser.setUserLastName("admin");
        Set<Role> adminRoles = new HashSet<>();
        adminRoles.add(adminRole);
        adminUser.setRole(adminRoles);
        userDao.save(adminUser);
        
        List<String> permissionNames = Arrays.asList("USER_MANAGEMENT", "ACCESS_MANAGEMENT","MASTER_DATA","OTHER_RULE_SET","NETWORK_PRICING_TICKETS","OTHER_TICKETS","SOT_VALIDATION","UPLOAD_SOT","DASHBOARD_ACCESS","USER_AUDIT" /* Add more permissions */);

        for (String permissionName : permissionNames) {
            Permission permission = permissionRepository.findByPermissionName(permissionName);
            if (permission == null) {
                permission = new Permission(permissionName);
                permissionRepository.save(permission);
            }
        }

//        User user = new User();
//        user.setUserName("raj123");
//        user.setUserPassword(getEncodedPassword("raj@123"));
//        user.setUserFirstName("raj");
//        user.setUserLastName("sharma");
//        Set<Role> userRoles = new HashSet<>();
//        userRoles.add(userRole);
//        user.setRole(userRoles);
//        userDao.save(user);
    }

    public User registerNewUser(User user, Set<Permission> permissions) {
    	
    	// Check if roles are provided in the JSON body
        Set<Role> userRoles = user.getRole();
        if (userRoles == null || userRoles.isEmpty()) {
            // If no roles are provided, set the default role to "User"
            Role defaultRole = roleDao.findById("User")
                    .orElseThrow(() -> new IllegalArgumentException("Default role 'User' not found"));
            userRoles = new HashSet<>();
            userRoles.add(defaultRole);
        }
        
     // Set permissions if provided
        if (permissions != null && !permissions.isEmpty()) {
            // Associate permissions with the user roles
            userRoles.forEach(role -> role.setPermissions(permissions));
        }
        user.setRole(userRoles);
        user.setUserPassword(getEncodedPassword(user.getUserPassword()));

        return userDao.save(user);
    }

    public String getEncodedPassword(String password) {
        return passwordEncoder.encode(password);
    }
    
    public Optional<ReqRes> updateUserByUsername(String userName, User updatedUser) {
        Optional<User> optionalUser = userDao.findByUserName(userName);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            // Update the user fields here
            user.setUserFirstName(updatedUser.getUserFirstName());
            user.setUserLastName(updatedUser.getUserLastName());
            user.setUserPassword(getEncodedPassword(user.getUserPassword()));
            user.setRole(updatedUser.getRole());
            userDao.save(user);
            return Optional.of(new ReqRes(HttpStatus.OK.value(), "", "User updated successfully"));
        } else {
            return Optional.of(new ReqRes(HttpStatus.NOT_FOUND.value(), "User not found", ""));
        }
    }
    
    public Optional<ReqRes> deleteUserByUsername(String userName) {
        Optional<User> optionalUser = userDao.findByUserName(userName);
        if (optionalUser.isPresent()) {
        	 User user = optionalUser.get();
        	// Delete associated roles from user_role table
            user.setRole(null); // Remove all roles from the user
            userDao.save(user); // Save the user without roles, which will cascade the deletion of associated roles
        	userDao.delete(optionalUser.get());
            return Optional.of(new ReqRes(HttpStatus.OK.value(), "", "User deleted successfully"));
        } else {
            return Optional.of(new ReqRes(HttpStatus.NOT_FOUND.value(), "User not found", ""));
        }
    }
}
