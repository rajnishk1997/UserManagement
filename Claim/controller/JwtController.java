package com.optum.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.optum.entity.JwtRequest;
import com.optum.entity.JwtResponse;
import com.optum.service.JwtService;

@RestController
@CrossOrigin
public class JwtController {

    @Autowired
    private JwtService jwtService;

    @PostMapping({"/login"})
    public JwtResponse createJwtToken(@RequestBody JwtRequest jwtRequest) throws Exception {
        return jwtService.createJwtToken(jwtRequest);
    }
}
