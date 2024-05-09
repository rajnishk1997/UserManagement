package com.optum.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.optum.dao.PermissionDao;
import com.optum.dao.ReqRes;
import com.optum.dao.RoleDao;
import com.optum.dao.UserDao;
import com.optum.entity.Permission;
import com.optum.entity.Role;
import com.optum.entity.User;

import java.util.Optional;
import java.util.Arrays;
import java.util.Collections;
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

    @Transactional
    public void initRoleAndUser() {
    	
    	List<String> permissionNames = Arrays.asList("USER MANAGEMENT", "ACCESS MANAGEMENT","MASTER DATA","OTHER RULE SET","NETWORK PRICING TICKETS","OTHER TICKETS","SOT VALIDATION","UPLOAD SOT","DASHBOARD ACCESS","USER AUDIT" /* Add more permissions */);

        for (String permissionName : permissionNames) {
            Permission permission = permissionRepository.findByPermissionName(permissionName);
            if (permission == null) {
                permission = new Permission(permissionName);
                permissionRepository.save(permission);
            }
        }
    	//Role 1- Admin
        Role adminRole = new Role();
        adminRole.setRoleName("Admin");
        adminRole.setRoleDescription("Admin role");
        roleDao.save(adminRole);
//        List<Permission>permissions = permissionRepository.findAllByPermissionNameIn(
//        		Arrays.asList("USER MANAGEMENT", "ACCESS MANAGEMENT","MASTER DATA","OTHER RULE SET")
//        		);
        //dynamically fetch all permissions from DB and assign them to Admin Role:
        List<Permission> permissions = permissionRepository.findAll();
        adminRole.setPermissions(new HashSet<>(permissions));
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
        adminUser.setRoles(adminRoles);
        userDao.save(adminUser);
        
        

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

    public User registerNewUser(User user, Set<Permission> providedPermissions) {
        // Retrieve existing roles from the database
        Set<Role> userRoles = new HashSet<>();
        Set<Permission> retrievedPermissions = new HashSet<>();
        for (Role role : user.getRoles()) {
            Role existingRole = roleDao.findByRoleName(role.getRoleName());
            if (existingRole != null) {
                userRoles.add(existingRole);
                retrievedPermissions.addAll(existingRole.getPermissions());
            } else {
                throw new IllegalArgumentException("Role '" + role.getRoleName() + "' not found");
            }
        }

        // Associate permissions with the user roles
        userRoles.forEach(role -> role.setPermissions(retrievedPermissions));

        // Set the roles for the user
        user.setRoles(userRoles);

        // Set encoded password for the user
        user.setUserPassword(getEncodedPassword(user.getUserPassword()));

        // Save the user with the existing roles and permissions
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
            user.setRoles(updatedUser.getRoles());
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
            user.setRoles(null); // Remove all roles from the user
            userDao.save(user); // Save the user without roles, which will cascade the deletion of associated roles
        	userDao.delete(optionalUser.get());
            return Optional.of(new ReqRes(HttpStatus.OK.value(), "", "User deleted successfully"));
        } else {
            return Optional.of(new ReqRes(HttpStatus.NOT_FOUND.value(), "User not found", ""));
        }
    }

    public List<User> getAllUsers() {
        try {
            return userDao.findAll();
        } catch (Exception e) {
            // Log the exception or handle it as needed
            return Collections.emptyList(); // Return an empty list in case of an error
        }
    }

    public List<User> searchUsersByKeyword(String keyword) {
        return userDao.findByUserNameContainingIgnoreCaseOrUserFirstNameContainingIgnoreCase(keyword, keyword);
    }
    
    public List<User> findByUserName(String userName) {
    	Optional<User> userOptional = userDao.findByUserName(userName);
        return userOptional.map(Collections::singletonList).orElseGet(Collections::emptyList);
    }

    public List<User> findByUserFirstName(String userFirstName) {
        return userDao.findByUserFirstName(userFirstName);
    }

}
