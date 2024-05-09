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
import org.springframework.web.bind.annotation.RestController;

import com.optum.dao.ReqRes;
import com.optum.entity.*;
import com.optum.service.UserService;

import java.util.HashSet;
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
    public User registerNewUser(@RequestBody User user) {
    	 Set<Permission> permissions = new HashSet<>();
    	    for (Role role : user.getRoles()) {
    	        permissions.addAll(role.getPermissions());
    	    }
        return userService.registerNewUser(user,permissions);
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
    public ResponseEntity<ReqRes> updateUser(@PathVariable String userName, @RequestBody User updatedUser) {
        java.util.Optional<ReqRes> optionalUser = userService.updateUserByUsername(userName, updatedUser);
        if (optionalUser.isPresent()) {
            return ResponseEntity.ok(new ReqRes(HttpStatus.OK.value(), "", "User updated successfully"));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                 .body(new ReqRes(HttpStatus.NOT_FOUND.value(), "User not found", ""));
        }
    }
    
    @DeleteMapping("/admin/delete/{userName}")
    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<ReqRes> deleteUser(@PathVariable String userName) {
        java.util.Optional<ReqRes> optionalReqRes = userService.deleteUserByUsername(userName);
        return optionalReqRes.map(reqRes -> new ResponseEntity<>(reqRes, HttpStatus.OK))
                             .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }
}
