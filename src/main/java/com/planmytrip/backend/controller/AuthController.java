package com.planmytrip.backend.controller;

import com.planmytrip.backend.model.User;
import com.planmytrip.backend.repository.UserRepository;
import com.planmytrip.backend.repository.VerificationTokenRepository;
import com.planmytrip.backend.service.UserService;
import com.planmytrip.backend.util.JwtTokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserService userService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private VerificationTokenRepository tokenRepository;
    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody User user) {
        return userService.loginUser(user);
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody User user) throws Exception {
        return userService.registerUser(user);
    }

    @GetMapping("/verify")
    public ResponseEntity<String> verifyEmail(@RequestParam("token") String token) {
        return userService.verifyEmail(token);
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<?> resendVerification(@RequestBody User user) {
        return userService.resendVerification(user);
    }

    @PostMapping("/request-reset-password")
    public ResponseEntity<?> requestResetPassword(@RequestBody User user) {
        return userService.requestResetPassword(user);
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody User user) {
        return userService.resetPassword(user);
    }

    @PostMapping("/validate-token")
    public ResponseEntity<?> validateToken(@RequestHeader("Authorization") String authorizationHeader) {
        String token = null;
        String email = null;
        Map<String, Object> response = new HashMap<>();

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            token = authorizationHeader.substring(7);
            email = jwtTokenUtil.extractUsername(token);
        }

        if (email != null) {
            User user = userRepository.findByEmail(email).orElse(null);
            if (user != null && jwtTokenUtil.validateToken(token, user)) {
                response.put("valid", true);
                response.put("email", user.getEmail());
                response.put("name", user.getName());
                return ResponseEntity.ok(response);
            }
        }

        response.put("valid", false);
        return ResponseEntity.ok(response);
    }


}
