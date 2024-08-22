package com.planmytrip.backend.service;

import com.planmytrip.backend.model.User;
import com.planmytrip.backend.model.VerificationToken;
import com.planmytrip.backend.repository.UserRepository;
import com.planmytrip.backend.repository.VerificationTokenRepository;
import com.planmytrip.backend.util.JwtTokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class UserService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private VerificationTokenRepository tokenRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JavaMailSender mailSender;
    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    public ResponseEntity<?> loginUser(User user) {
        Map<String, String> response = new HashMap<>();
        try {
            Optional<User> userOpt = userRepository.findByEmail(user.getEmail());
            if(userOpt.isEmpty()) {
                response.put("message", "Invalid Email");
                return ResponseEntity.badRequest().body(response);
            }
            User u = userOpt.get();
            if(!passwordEncoder.matches(user.getPassword(), u.getPassword())) {
                response.put("message", "Invalid Password");
                return ResponseEntity.badRequest().body(response);
            }
            if(!u.isEnabled()) {
                sendVerificationEmail(u);
                response.put("message", "Email is not verified. Please verify your email. Verification link sent to "+u.getEmail());
                return ResponseEntity.badRequest().body(response);
            }
            String token = jwtTokenUtil.generateToken(u);
            response.put("message", "success");
            response.put("token", token);
            response.put("name", u.getName());
            return ResponseEntity.ok().body(response);
        } catch (Exception e) {
            response.put("message", e.getLocalizedMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    public ResponseEntity<?> registerUser(User user) {
        Map<String, String> response = new HashMap<>();
        try {
            Optional<User> userOpt = userRepository.findByEmail(user.getEmail());
            if (userOpt.isPresent()) {
                response.put("message", "User already exists with this email.");
                return ResponseEntity.badRequest().body(response);
            }
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            userRepository.save(user);
            sendVerificationEmail(user);
            response.put("message", "Registration successful. Please check your email for verification.");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("message", e.getLocalizedMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(response);
        }
    }

    public ResponseEntity<?> resendVerification(User user) {
        Map<String, String> response = new HashMap<>();
        try {
            Optional<User> userOpt = userRepository.findByEmail(user.getEmail());
            if (userOpt.isEmpty()) {
                response.put("message", "No user found with email "+ user.getEmail());
                return ResponseEntity.badRequest().body(response);
            }
            User u = userOpt.get();
            if (u.isEnabled()) {
                response.put("message", "User already verified");
               return ResponseEntity.badRequest().body(response);
            }
            sendVerificationEmail(user);
            response.put("message", "Please check your email for verification.");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("message", e.getLocalizedMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(response);
        }
    }

    private void sendVerificationEmail(User user) {
        String token = UUID.randomUUID().toString();
        VerificationToken verificationToken = new VerificationToken(token, user);
        tokenRepository.save(verificationToken);

        String recipientAddress = user.getEmail();
        String subject = "Email Verification";
        String confirmationUrl = "http://localhost:8080/auth/verify?token=" + token;

        SimpleMailMessage email = new SimpleMailMessage();
        email.setTo(recipientAddress);
        email.setSubject(subject);
        email.setText("Please verify your email by clicking the following link: " + confirmationUrl +" \nLink expires in 24 hours");
        mailSender.send(email);
    }

    public ResponseEntity<String> verifyEmail(String token) {
        try {
            Optional<VerificationToken> verificationTokenOpt = tokenRepository.findByToken(token);

            if (verificationTokenOpt.isEmpty()) {
                return ResponseEntity.badRequest().body("Invalid token.");
            }

            VerificationToken verificationToken = verificationTokenOpt.get();

            if (verificationToken.getExpiryDate().isBefore(LocalDateTime.now())) {
                return ResponseEntity.badRequest().body("Token has expired.");
            }

            User user = verificationToken.getUser();
            user.setEnabled(true);
            userRepository.save(user);
            tokenRepository.delete(verificationToken);
            return ResponseEntity.ok("Email verified successfully. You can close the tab");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(e.getLocalizedMessage());
        }
    }

    public ResponseEntity<?> requestResetPassword(User user) {
        Map<String, String> response = new HashMap<>();
        try {
            User u = userRepository.findByEmail(user.getEmail()).orElse(null);
            if (u == null) {
                response.put("message", "Email does not exist");
                return ResponseEntity.badRequest().body(response);
            }
            sendResetPasswordCode(u);
            response.put("message", "Please check your email for reset password code");
            return ResponseEntity.ok(response);
        } catch (Exception e){
            response.put("message", e.getLocalizedMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    private void sendResetPasswordCode(User user) {
        Random random = new Random();
        int randomNumber = 100000 + random.nextInt(900000);
        String token = String.valueOf(randomNumber);
        VerificationToken verificationToken = new VerificationToken(token, user);
        tokenRepository.save(verificationToken);

        String recipientAddress = user.getEmail();
        String subject = "Reset Password code";

        SimpleMailMessage email = new SimpleMailMessage();
        email.setTo(recipientAddress);
        email.setSubject(subject);
        email.setText("Please use the following code to reset the password: " + token +" \n\nCode expires in 24 hours");
        mailSender.send(email);
    }

    public ResponseEntity<?> resetPassword(User user) {
        Map<String, String> response = new HashMap<>();
        try {
            User u = userRepository.findByEmail(user.getEmail()).orElse(null);
            if (u == null) {
                response.put("message", "Email does not exist");
                return ResponseEntity.badRequest().body(response);
            }
            VerificationToken verificationToken = tokenRepository.findByToken(String.valueOf(user.getCode())).orElse(null);
            if(verificationToken == null) {
                response.put("message", "Invalid Code");
                return ResponseEntity.badRequest().body(response);
            }
            u.setPassword(passwordEncoder.encode(user.getPassword()));
            u.setEnabled(true);
            userRepository.save(u);
            tokenRepository.delete(verificationToken);
            response.put("message", "Password updated");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("message", e.getLocalizedMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + username));

        return new org.springframework.security.core.userdetails.User(user.getEmail(), user.getPassword(), new ArrayList<>());
    }

}
