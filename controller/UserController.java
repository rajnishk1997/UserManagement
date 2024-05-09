package com.optum.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.optum.dao.ReqRes;
import com.optum.entity.*;
import com.optum.service.UserService;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;

@RestController
public class UserController {

    @Autowired
    private UserService userService;

    @PostConstruct   //PostConstruct as I wish to run this code once the compilation is done.
    public void initRoleAndUser() {
        userService.initRoleAndUser();
    }

    @PostMapping({"/registerNewUser"})
    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<ResponseWrapper<User>> registerNewUser(@RequestBody User user) {
        try {
            Set<Permission> permissions = new HashSet<>();
            for (Role role : user.getRoles()) {
                permissions.addAll(role.getPermissions());
            }
            User registeredUser = userService.registerNewUser(user, permissions);
            ReqRes reqRes = new ReqRes(HttpStatus.CREATED.value(), "", "User registered successfully");
            return ResponseEntity.status(HttpStatus.CREATED).body(new ResponseWrapper<>(registeredUser, reqRes));
        } catch (Exception e) {
            ReqRes reqRes = new ReqRes(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Internal Server Error", "An error occurred while registering the user");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ResponseWrapper<>(null, reqRes));
        }
    }

    
    
    // Method to match exact user
    @GetMapping("/admin/get-all-users")
    public ResponseEntity<ResponseWrapper<List<User>>> getAllUsers(
            @RequestParam(required = false) String userName,
            @RequestParam(required = false) String userFirstName) {
        try {
            List<User> userList;
            if (userName != null) {
                userList = userService.findByUserName(userName);
            } else if (userFirstName != null) {
                userList = userService.findByUserFirstName(userFirstName);
            } else {
                userList = userService.getAllUsers();
            }
            
            ReqRes reqRes;
            if (userList.isEmpty()) {
                reqRes = new ReqRes(HttpStatus.NOT_FOUND.value(), "Users not found", "No users found in the database");
            } else {
                reqRes = new ReqRes(HttpStatus.OK.value(), null, "Users retrieved successfully");
            }
            return ResponseEntity.ok(new ResponseWrapper<>(userList, reqRes));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
    
    // Method to match useCases:
    @GetMapping("/admin/get-all-users-case")
    public ResponseEntity<ResponseWrapper<List<User>>> getAllUsersCases(
            @RequestParam(required = false) String keyword) {
        try {
            List<User> userList;
            if (keyword != null && !keyword.isEmpty()) {
                userList = userService.searchUsersByKeyword(keyword);
            } else {
                userList = userService.getAllUsers();
            }

            ReqRes reqRes;
            if (userList.isEmpty()) {
                reqRes = new ReqRes(HttpStatus.NOT_FOUND.value(), "Users not found", "No users found in the database");
            } else {
                reqRes = new ReqRes(HttpStatus.OK.value(), null, "Users retrieved successfully");
            }
            return ResponseEntity.ok(new ResponseWrapper<>(userList, reqRes));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping({"/forAdmin"})
    @PreAuthorize("hasRole('Admin')")
    public String forAdmin(){
        return "This URL is only accessible to the admin";
    }

    @GetMapping({"/forUser"})
    @PreAuthorize("hasRole('User')")
    public String forUser(){
        return "This URL is only accessible to the user";
    }
    
    @PutMapping("/admin/update/{userName}")
    public ResponseEntity<ResponseWrapper<ReqRes>> updateUser(@PathVariable String userName, @RequestBody User updatedUser) {
        try {
            java.util.Optional<ReqRes> optionalUser = userService.updateUserByUsername(userName, updatedUser);
            if (optionalUser.isPresent()) {
                ReqRes reqRes = new ReqRes(HttpStatus.OK.value(), "", "User updated successfully");
                return ResponseEntity.ok(new ResponseWrapper<>(reqRes, reqRes));
            } else {
                ReqRes reqRes = new ReqRes(HttpStatus.NOT_FOUND.value(), "User not found", "");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ResponseWrapper<>(reqRes, reqRes));
            }
        } catch (Exception e) {
            ReqRes reqRes = new ReqRes(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Internal Server Error", "An error occurred while updating the user");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ResponseWrapper<>(reqRes, reqRes));
        }
    }

    
    @DeleteMapping("/admin/delete/{userName}")
    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<ResponseWrapper<ReqRes>> deleteUser(@PathVariable String userName) {
        try {
            java.util.Optional<ReqRes> optionalReqRes = userService.deleteUserByUsername(userName);
            if (optionalReqRes.isPresent()) {
                ReqRes reqRes = optionalReqRes.get();
                return ResponseEntity.ok(new ResponseWrapper<>(reqRes, reqRes));
            } else {
                ReqRes reqRes = new ReqRes(HttpStatus.NOT_FOUND.value(), "User not found", "");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ResponseWrapper<>(reqRes, reqRes));
            }
        } catch (Exception e) {
            ReqRes reqRes = new ReqRes(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Internal Server Error", "An error occurred while deleting the user");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ResponseWrapper<>(reqRes, reqRes));
        }
    }

}
