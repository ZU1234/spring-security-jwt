package com.intesasoft.controller;

import com.intesasoft.request.UserLoginRequest;
import com.intesasoft.request.UserRequest;
import com.intesasoft.response.UserResponse;
import com.intesasoft.response.UserTokenResponse;
import com.intesasoft.service.AuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class AuthenticationController {
    @Autowired
    private AuthenticationService authenticationService;

    @PostMapping(value = "/register")
    public ResponseEntity<UserTokenResponse> save(@RequestBody UserRequest userDto) {
        return ResponseEntity.ok(authenticationService.save(userDto));
    }

    @GetMapping("/user")
    public List<UserResponse> getList() {
        return authenticationService.getList();
    }

    @PostMapping("/user/me")
    public ResponseEntity<UserResponse> registerUser() {
        return ResponseEntity.ok(authenticationService.getUserResponse());
    }

    @PostMapping("/login")
    public ResponseEntity<UserTokenResponse> login(@RequestBody UserLoginRequest request) {
        return ResponseEntity.ok(authenticationService.login(request));
    }


}
