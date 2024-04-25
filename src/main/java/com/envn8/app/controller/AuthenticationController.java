package com.envn8.app.controller;

// import java.util.HashSet;
import java.util.List;
// import java.util.Set;
import java.util.stream.Collectors;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.envn8.app.models.User;
import com.envn8.app.payload.request.LogInRequest;
import com.envn8.app.payload.response.MessageResponse;
import com.envn8.app.repository.UserRepository;
import com.envn8.app.payload.request.*;
import com.envn8.app.service.UserDetailsImpl;
import com.envn8.app.security.jwt.JwtUtils;
import com.envn8.app.payload.response.UserInfoResponse;
// import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

//import AuthenticationManagerBuilder
// import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;

// @CrossOrigin(origins = "http://localhost:3000")
@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class AuthenticationController {

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    UserRepository userRepository;
    @Autowired
    PasswordEncoder encoder;
    @Autowired
    JwtUtils jwtUtils;

    @Autowired
    public AuthenticationController(AuthenticationManager authenticationManager, UserRepository userRepository,
            PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        // this.roleRepository = roleRepository;
        this.encoder = passwordEncoder;
        // this.jwtGenerator = jwtGenerator;
    }

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LogInRequest loginRequest) {

System.out.println(
                "-**************************************************************\n-QQQQQQQQQQQQQQQQQQQQQQQQQQQQ\n******************************\n");
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));
        // System.out.println("In here, Authentication: " + authentication);
        // System.out.println("Username is " + loginRequest.getUsername());
        // System.out.println("Password is " + loginRequest.getPassword());
        // System.out.println(
        //         "-**************************************************************\n-QQQQQQQQQQQQQQQQQQQQQQQQQQQQ\n******************************\n");
        SecurityContextHolder.getContext().setAuthentication(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        ResponseCookie jwtCookie = jwtUtils.generateJwtCookie(userDetails);

        List<String> roles = userDetails.getAuthorities().stream()
                .map(item -> item.getAuthority())
                .collect(Collectors.toList());

        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
                .body(new UserInfoResponse(userDetails.getId(),
                        userDetails.getUsername(),
                        userDetails.getEmail(),
                        roles));
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignUpRequest signUpRequest) {
        if (userRepository.existsByUsername(signUpRequest.getUsername())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Username is already taken!"));
        }

        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Email is already in use!"));
        }
        // handle the password length, 8 characters long
        if (signUpRequest.getPassword().length() < 8) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Password must be at least 8 characters long!"));
        }

        // handle the password pattern satisfaction
        if (!signUpRequest.getPassword().matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).*$")) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse(
                            "Error: Password must contain at least one uppercase letter, one lowercase letter, and one number!"));
        }

        // handle the password and confirm password mismatch
        if (!signUpRequest.getPassword().equals(signUpRequest.getConfirmPassword())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Passwords do not match!"));
        }
        // Create new user's account
        User user = new User(signUpRequest.getUsername(),
                encoder.encode(signUpRequest.getPassword()));

        user.setFirstName(signUpRequest.getFirstName());
        user.setLastName(signUpRequest.getLastName());
        user.setEmail(signUpRequest.getEmail());
        user.setPassword(encoder.encode(signUpRequest.getPassword()));
        // user.setRoles(signUpRequest.getRoles());

        /*
         * 
         * // some logs
         * print the received username, password, firstName, lastName, email
         * System.out.println("Username: " + signUpRequest.getUsername());
         * System.out.println("Password: " + signUpRequest.getPassword());
         * System.out.println("First Name: " + signUpRequest.getFirstName());
         * System.out.println("Last Name: " + signUpRequest.getLastName());
         * System.out.println("Email: " + signUpRequest.getEmail());
         * 
         */

        // Role Handling code is commented out
        // Set<String> strRoles = signUpRequest.getRoles();
        // Set<Role> roles = new HashSet<>();

        // if (strRoles == null) {
        // Role userRole = roleRepository.findByName(ERole.ROLE_USER)
        // .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
        // roles.add(userRole);
        // } else {
        // strRoles.forEach(role -> {
        // switch (role) {
        // case "admin":
        // Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN)
        // .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
        // roles.add(adminRole);

        // break;
        // case "mod":
        // Role modRole = roleRepository.findByName(ERole.ROLE_MODERATOR)
        // .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
        // roles.add(modRole);

        // break;
        // default:
        // Role userRole = roleRepository.findByName(ERole.ROLE_USER)
        // .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
        // roles.add(userRole);
        // }
        // });
        // }

        // user.setRoles(roles);
        userRepository.save(user);
        return ResponseEntity.ok(new MessageResponse("Man, User registered successfully!"));
    }
}
