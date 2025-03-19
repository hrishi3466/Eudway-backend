package com.eduway.auth.controller;

import com.eduway.auth.dto.AuthRequest;
import com.eduway.auth.model.User;
import com.eduway.auth.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserService userService;



    @Autowired
    private AuthenticationManager authenticationManager;

    @PostMapping("/signup")
    public String registerUser(@RequestBody User user) {
        return userService.registerUser(user);
    }

    @PostMapping("/signin")
    public String loginUser(@RequestBody AuthRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        if (authentication.isAuthenticated()) {
            return "Login successful! You are now authenticated.";
        } else {
            return "Invalid username or password!";
        }
    }

    @GetMapping("/welcome")
    public String welcome(Principal principal) {
        return "Welcome, " + principal.getName() + "! You are signed in.";
    }

    @GetMapping("/signout")
    public String goodbye() {
        return "You have signed out successfully!";
    }
}
