package com.optum.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.optum.dao.PermissionDao;
import com.optum.dao.ReqRes;
import com.optum.dao.RoleDao;
import com.optum.dao.RolePermissionDao;
import com.optum.dao.UserDao;
import com.optum.dao.UserRoleDao;
import com.optum.entity.Permission;
import com.optum.entity.Role;
import com.optum.entity.RolePermission;
import com.optum.entity.User;
import com.optum.entity.UserRole;
import com.optum.exception.UserRegistrationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Optional;
import java.util.Random;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
	private UserRoleDao userRoleDao;

	@Autowired
	private RolePermissionDao rolePermissionDao;

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
		// Role 1- Admin
		Role adminRole = new Role();
		adminRole.setRoleName("Admin");
		adminRole.setRoleDescription("Admin role");
		roleDao.save(adminRole);
		// dynamically fetch all permissions from DB and assign them to Admin Role:
		List<Permission> permissions = permissionRepository.findAll();
		adminRole.setPermissions(new HashSet<>(permissions));
		roleDao.save(adminRole);

		// User 1 - Admin User
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

		// Now Create User-Role
		// Step 2: Retrieve the permissions associated with the Admin role
		List<Permission> permissions1 = permissionRepository.findAll();

		// Step 3: Create a new UserRole object
		UserRole userRole = new UserRole();

		// Step 4: Set the necessary fields for the UserRole object
		userRole.setRoleRid(adminRole.getRoleRid());
		userRole.setUserRid(adminUser.getUserRid());

		// Step 5: Save the UserRole object using UserRoleDao
		userRoleDao.save(userRole);

		// Now Create RolePermission entries
		for (Permission permission : permissions) {
			RolePermission rolePermission = new RolePermission();
			rolePermission.setRoleId(adminRole.getRoleRid()); // Using Role ID
			rolePermission.setPermissionId(permission.getPermissionRid());
			rolePermissionDao.save(rolePermission);
		}

		// Role 2- User
		// Find existing permissions by name
		List<String> permissionNames = Arrays.asList("USER MANAGEMENT", "ACCESS MANAGEMENT");
		List<Permission> existingPermissions = permissionRepository.findAllByPermissionNameIn(permissionNames);

		// Create a user role
		Role userRole1 = new Role();
		userRole1.setRoleName("User");
		userRole1.setRoleDescription("Default role for newly created record");

		// Initialize the permissions set in the user role
		userRole1.setPermissions(new HashSet<>());

		// Filter out existing permissions that are not already associated with the user
		// role to avoid duplicates
		Set<Permission> existingPermissionsToAssociate = existingPermissions.stream()
				.filter(permission -> !userRole1.getPermissions().contains(permission)).collect(Collectors.toSet());

		// Associate the filtered permissions with the user role
		userRole1.getPermissions().addAll(existingPermissionsToAssociate);

		// Save the user role
		roleDao.save(userRole1);

		// User 1 - Admin User
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
			
			if (userDao.existsByUserMobile(user.getUserMobile())) {
	            throw new IllegalArgumentException("User already exists with mobile number: " + user.getUserMobile());
	        }
	        if (userDao.existsByUserEmail(user.getUserEmail())) {
	            throw new IllegalArgumentException("User already exists with email: " + user.getUserEmail());
	        }
			// Null checks
			if (user == null || user.getRoles() == null || user.getRoles().isEmpty()) {
				throw new IllegalArgumentException("Invalid user data");
			}

			// Retrieve existing roles from the database
			Set<Role> userRoles1 = new HashSet<>();
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
					userRoles1.add(existingRole);
				} else {
					throw new IllegalArgumentException("Role '" + role.getRoleName() + "' not found");
				}
			}

			// Set the roles for the user
			user.setRoles(userRoles1);

			// Set encoded password for the user

			String firstName = user.getUserFirstName();
			String middleName = user.getUserMiddleName();
			String lastName = user.getUserLastName();

			String generatedPassword = generateSystemPassword(firstName, lastName);
			String generatedUserName = generateUserName(firstName, lastName);
			String userFullName = createFullName(firstName, middleName, lastName);

			// Set the generated password for the user
			user.setUserPassword(generatedPassword);
			user.setUserName(generatedUserName);
			user.setUserFullName(userFullName);

			// Also Map User and Role with UserRole Table:
			// Set roles for the user
			Set<Role> userRoles = new HashSet<>();
			for (Role role : user.getRoles()) {
				Role existingRole = roleDao.findByRoleName(role.getRoleName());
				if (existingRole != null) {
					userRoles.add(existingRole);
				} else {
					throw new IllegalArgumentException("Role '" + role.getRoleName() + "' not found");
				}
			}
			user.setRoles(userRoles);
			
			// Set created date, created by, modified date, and modified by
	        Date currentDate = new Date();
	        user.setCreatedDate(currentDate);
	        user.setCreatedBy(user.getCurrentUserId());

			// Save the user with the modified roles
			User savedUser = userDao.save(user);

			// Map User's u_rid and Role's r_rid with UserRole Table
			for (Role role : userRoles) {
				UserRole userRole1 = new UserRole();
				userRole1.setUserRid(savedUser.getUserRid()); // Assuming getId() returns the user ID
				userRole1.setRoleRid(role.getRoleRid()); // Assuming getId() returns the role ID
				// Save userRole to your database
				userRole1.setCreatedDate(currentDate);
				userRole1.setCreatedBy(user.getCurrentUserId());
				userRoleDao.save(userRole1); // Assuming userRoleDao is your data access object for UserRole
			}
			
			 // Concatenate role names
	        StringBuilder roleNamesBuilder = new StringBuilder();
	        for (Role role : user.getRoles()) {
	            if (roleNamesBuilder.length() > 0) {
	                roleNamesBuilder.append(", ");
	            }
	            roleNamesBuilder.append(role.getRoleName());
	        }
	        String roleNames = roleNamesBuilder.toString();
	        user.setRoleNames(roleNames);

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
	
	
	public String generateUserName(String firstName, String lastName) {
	    // Validate input parameters
	    if (firstName == null || lastName == null || firstName.isEmpty() || lastName.isEmpty()) {
	        throw new IllegalArgumentException("First name and last name cannot be null or empty.");
	    }

	    // Generate a random number between 1000 and 9999
	    Random random = new Random();
	    int randomNumber = random.nextInt(9000) + 1000; // To ensure it's a 4-digit number

	    // Concatenate the first name, last name, and random number to form the username
	    String userName = firstName.toLowerCase() + lastName.toLowerCase() + randomNumber;
	    return userName;
	}


	String generateSystemPassword(String firstName, String lastName) {
		// Check if last name is null or empty
		if (lastName == null || lastName.isEmpty()) {
			throw new IllegalArgumentException("Last name cannot be null or empty.");
		}

		// Generate a random number between 0 and 9999
		Random random = new Random();
		int randomNumber = random.nextInt(1000);

		// Choose a random symbol from a predefined set
		String symbols = "!@#$%^&*()_-+=<>?/[]{},.";
		char randomSymbol = symbols.charAt(random.nextInt(symbols.length()));

		// Concatenate the parts to form the password
		String generatedPassword = firstName + lastName + randomNumber + randomSymbol;

		return generatedPassword;
	}

	public String getEncodedPassword(String password) {
		return passwordEncoder.encode(password);
	}
	
	public String createFullName(String firstName, String middleName, String lastName) {
        StringBuilder fullName = new StringBuilder();

        if (firstName != null) {
            fullName.append(firstName);
        }

        if (middleName != null) {
            if (fullName.length() > 0) {
                fullName.append(" ");
            }
            fullName.append(middleName);
        }

        if (lastName != null) {
            if (fullName.length() > 0) {
                fullName.append(" ");
            }
            fullName.append(lastName);
        }

        return fullName.toString();
    }

//    public Optional<User> updateUserByUsername(String userName, User updatedUser) {
//        Optional<User> optionalUser = userDao.findByUserName(userName);
//        if (optionalUser.isPresent()) {
//            User user = optionalUser.get();
//            // Update other user fields if needed
//            user.setUserFirstName(updatedUser.getUserFirstName());
//            user.setUserLastName(updatedUser.getUserLastName());
//            user.setUserMiddleName(updatedUser.getUserMiddleName());
//            user.setUserMobile(updatedUser.getUserMobile());
//            user.setUserEmail(updatedUser.getUserEmail());
//            user.setUserDesignation(updatedUser.getUserDesignation());
//
//            // Fetch the existing roles for the user
//            Set<Role> existingRoles = user.getRoles();
//
//            // Fetch the existing role from the database by name
//            Role updatedRole = updatedUser.getRoles().iterator().next(); // Assuming only one role is updated
//
//            // Check if the updated role already exists in the user's roles
//            boolean roleExists = existingRoles.stream().anyMatch(role -> role.getRoleName().equals(updatedRole.getRoleName()));
//
//            if (roleExists) {
//                // Update the user's roles with the existing ones
//                user.setRoles(existingRoles);
//                userDao.save(user);
//                return Optional.of(user);
//            } else {
//                // Role does not exist in the user's roles, return empty Optional
//                return Optional.empty();
//            }
//        } else {
//            return Optional.empty(); // User not found
//        }
//    }

	public Optional<User> updateUserByUserId(Integer userId, User updatedUser) {
		Optional<User> optionalUser = userDao.findById(userId);
		if (optionalUser.isPresent()) {
			User user = optionalUser.get();
			// Update other user fields if needed
			user.setUserFirstName(updatedUser.getUserFirstName());
			user.setUserLastName(updatedUser.getUserLastName());
			user.setUserMiddleName(updatedUser.getUserMiddleName());
			user.setUserMobile(updatedUser.getUserMobile());
			user.setUserEmail(updatedUser.getUserEmail());
			user.setUserDesignation(updatedUser.getUserDesignation());

			// Fetch the existing roles for the user
			Set<Role> existingRoles = user.getRoles();

			// Fetch the existing role from the database by name
			Role updatedRole = updatedUser.getRoles().iterator().next(); // Assuming only one role is updated

			// Check if the updated role already exists in the user's roles
			boolean roleExists = existingRoles.stream()
					.anyMatch(role -> role.getRoleName().equals(updatedRole.getRoleName()));

			if (roleExists) {
				// Update the user's roles with the existing ones
				user.setRoles(existingRoles);
				userDao.save(user);
				return Optional.of(user);
			} else {
				// Role does not exist in the user's roles, return empty Optional
				return Optional.empty();
			}
		} else {
			return Optional.empty(); // User not found
		}
	}

//    public Optional<ReqRes> deleteUserByUsername(String userName) {
//        Optional<User> optionalUser = userDao.findByUserName(userName);
//        if (optionalUser.isPresent()) {
//        	 User user = optionalUser.get();
//        	// Delete associated roles from user_role table
//            user.setRoles(null); // Remove all roles from the user
//            userDao.save(user); // Save the user without roles, which will cascade the deletion of associated roles
//        	userDao.delete(optionalUser.get());
//            return Optional.of(new ReqRes(HttpStatus.OK.value(), "", "User deleted successfully"));
//        } else {
//            return Optional.of(new ReqRes(HttpStatus.NOT_FOUND.value(), "User not found", ""));
//        }
//    }

	public Optional<ReqRes> deleteUserByUserId(Integer userId) {
		Optional<User> optionalUser = userDao.findById(userId);
		if (optionalUser.isPresent()) {
			User user = optionalUser.get();
			// Delete associated roles from user_role table
			user.setRoles(null); // Remove all roles from the user
			userDao.save(user); // Save the user without roles, which will cascade the deletion of associated
								// roles
			userDao.delete(user);
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
		return userDao
				.findByUserFirstNameContainingIgnoreCaseOrUserMiddleNameContainingIgnoreCaseOrUserLastNameContainingIgnoreCase(
						keyword, keyword, keyword);

	}

//    public List<User> findByUserName(String userName) {
//    	Optional<User> userOptional = userDao.findByUserName(userName);
//        return userOptional.map(Collections::singletonList).orElseGet(Collections::emptyList);
//    }

	public List<User> findByUserFirstName(String userFirstName) {
		return userDao.findByUserFirstName(userFirstName);
	}

//	public User getUserByUsername(String userName) {
//		Optional<User> optionalUser = userDao.findByUserName(userName);
//        return optionalUser.orElse(null);
//	}

	public User getUserByNames(String name) {
		Optional<User> optionalUser = userDao
				.findByUserFirstNameContainingIgnoreCaseOrUserLastNameContainingIgnoreCaseOrUserMiddleNameContainingIgnoreCase(
						name, name, name);
		return optionalUser.orElse(null);
	}

	public List<User> findByUserLastName(String userLastName) {
		// TODO Auto-generated method stub
		return userDao.findByUserLastName(userLastName);
	}

}
