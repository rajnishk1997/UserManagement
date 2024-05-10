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
import com.optum.exception.UserRegistrationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Optional;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class UserService {
//	@Value("${permissions}")
//    private String permissionsName;
	
	 private static final Logger logger = LogManager.getLogger(UserService.class);
	
	@Autowired
    private PermissionDao permissionRepository;

    @Autowired
    private UserDao userDao;

    @Autowired
    private RoleDao roleDao;

    @Autowired
    private PasswordEncoder passwordEncoder;
    
//    @Transactional
//    public void initPermissions() {
//		 List<String> permissionNames = Arrays.asList(permissionsName.split(", "));
//	        for (String permissionName : permissionNames) {
//	            Permission permission = permissionRepository.findByPermissionName(permissionName);
//	            if (permission == null) {
//	                permission = new Permission(permissionName);
//	                permissionRepository.save(permission);
//	            }
//	        }
//		
//	}
    
    

    @Transactional
    public void initRoleAndUser() {
    	
//    	List<String> permissionNames = Arrays.asList("USER MANAGEMENT", "ACCESS MANAGEMENT","MASTER DATA","OTHER RULE SET","NETWORK PRICING TICKETS","OTHER TICKETS","SOT VALIDATION","UPLOAD SOT","DASHBOARD ACCESS","USER AUDIT" /* Add more permissions */);
//
//        for (String permissionName : permissionNames) {
//            Permission permission = permissionRepository.findByPermissionName(permissionName);
//            if (permission == null) {
//                permission = new Permission(permissionName);
//                permissionRepository.save(permission);
//            }
//        }
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
        adminUser.setUserName("admin");
        adminUser.setUserFirstName("admin1");
        adminUser.setUserPassword(getEncodedPassword("admin"));
        adminUser.setUserLastName("Sharma");
        adminUser.setUserMobile("9656789056");
        adminUser.setUserEmail("admin@gmail.com");
        adminUser.setUserDesignation("CFO");
        adminUser.setUserEmployeeId("E1941945");
        Set<Role> adminRoles = new HashSet<>();
        adminRoles.add(adminRole);
        adminUser.setRoles(adminRoles);
        userDao.save(adminUser);
        
        //User 1 - Admin User
//        User adminUser2 = new User();
//        adminUser2.setUserFirstName("admin1");
//        adminUser2.setUserPassword(getEncodedPassword("admin1"));
//        adminUser2.setUserLastName("Sharma");
//        adminUser2.setUserMobile("9656559056");
//        adminUser2.setUserEmail("admin1@gmail.com");
//        adminUser2.setUserDesignation("CFO");
//        adminUser2.setUserEmployeeId("E1941943");
//        Set<Role> adminRoles1 = new HashSet<>();
//        adminRoles1.add(adminRole);
//        adminUser2.setRoles(adminRoles1);
//        userDao.save(adminUser2);

//        User user = new User();
//        user.setUserFirstName("raj");
//        user.setUserPassword(getEncodedPassword("raj123"));
//        user.setUserLastName("sharma");
//        Set<Role> userRoles = new HashSet<>();
//        userRoles.add(userRole);
//        user.setRoles(userRoles);
//        userDao.save(user);
    }

    @Transactional
    public User registerNewUser(User user, Set<Permission> providedPermissions) {
    	try {
            // Null checks
            if (user == null || user.getRoles() == null || user.getRoles().isEmpty()) {
                throw new IllegalArgumentException("Invalid user data");
            }

         // Retrieve existing roles from the database
            Set<Role> userRoles = new HashSet<>();
            Role userRole = new Role();
         
            for (Role role : user.getRoles()) {
                
                // Find the existing role by name
                Role existingRole = roleDao.findByRoleName(role.getRoleName());
                if (existingRole != null) {
                    // Use the existing role from the database and associate it with the user
                	// Set permissions for the user role
                    if (providedPermissions != null && !providedPermissions.isEmpty()) {
                        // Only add the provided permissions to the user role
                        userRole.setPermissions(new HashSet<>(providedPermissions));
                    }
                    userRoles.add(existingRole);
                } else {
                    throw new IllegalArgumentException("Role '" + role.getRoleName() + "' not found");
                }
            }

            // Set the roles for the user
            user.setRoles(userRoles);

            // Set encoded password for the user
            user.setUserPassword(getEncodedPassword(user.getUserPassword()));

            // Save the user with the modified roles
            return userDao.save(user);
        } catch (IllegalArgumentException e) {
            // Log the error
            logger.error("Error registering new user: " + e.getMessage());
            // Rethrow the exception
            throw e;
        } catch (Exception e) {
            // Log the error
            logger.error("An error occurred while registering the user", e);
            // Wrap the exception in a custom application exception and rethrow
            throw new UserRegistrationException("An error occurred while registering the user", e);
        }
    }




    public String getEncodedPassword(String password) {
        return passwordEncoder.encode(password);
    }
    
    public Optional<User> updateUserByUsername(String userName, User updatedUser) {
        Optional<User> optionalUser = userDao.findByUserName(userName);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            // Update the user fields here
          //  user.setUserFirstName(updatedUser.getUserFirstName());
            user.setUserLastName(updatedUser.getUserLastName());
            user.setUserPassword(getEncodedPassword(updatedUser.getUserPassword()));
            user.setRoles(updatedUser.getRoles());
            userDao.save(user);
            return Optional.of(user);
        } else {
            return Optional.empty(); // User not found
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

	public User getUserByUsername(String userName) {
		Optional<User> optionalUser = userDao.findByUserName(userName);
        return optionalUser.orElse(null);
	}


}
