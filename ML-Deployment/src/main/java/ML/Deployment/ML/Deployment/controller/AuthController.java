package ML.Deployment.ML.Deployment.controller;

import ML.Deployment.ML.Deployment.exception.BadRequestException;
import ML.Deployment.ML.Deployment.model.User;
import ML.Deployment.ML.Deployment.payload.ApiResponse;
import ML.Deployment.ML.Deployment.payload.AuthResponse;
import ML.Deployment.ML.Deployment.payload.LoginRequest;
import ML.Deployment.ML.Deployment.payload.SignUpRequest;
import ML.Deployment.ML.Deployment.repository.UserRepository;
import ML.Deployment.ML.Deployment.security.TokenProvider;
import com.fasterxml.jackson.core.JsonProcessingException; // Import Jackson exception
import com.fasterxml.jackson.databind.ObjectMapper; // Import ObjectMapper
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders; // Import HttpHeaders
import org.springframework.http.HttpStatus; // Import HttpStatus
import org.springframework.http.MediaType; // Import MediaType
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private TokenProvider tokenProvider;

    @Autowired
    private ObjectMapper objectMapper; // Autowire Jackson's ObjectMapper


    @PostMapping("/login")
    public ResponseEntity<String> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        logger.info("Attempting authentication for user: {}", loginRequest.getEmail());

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getEmail(),
                        loginRequest.getPassword()
                )
        );
        logger.info("Authentication successful for user: {}", loginRequest.getEmail());

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String token = tokenProvider.createToken(authentication);
        logger.info("Generated JWT Token (length): {}", token != null ? token.length() : "null");

        AuthResponse authResponse = new AuthResponse(token);
        logger.info("Created AuthResponse object");

        String jsonResponse;
        try {

            jsonResponse = objectMapper.writeValueAsString(authResponse);
            logger.info("Successfully serialized AuthResponse to JSON string.");
        } catch (JsonProcessingException e) {
            logger.error("Error serializing AuthResponse to JSON", e);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.TEXT_PLAIN)
                    .body("Error creating JSON response");
        }


        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);


        return new ResponseEntity<>(jsonResponse, headers, HttpStatus.OK);

    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignUpRequest signUpRequest) {
        logger.info("Attempting registration for user: {}", signUpRequest.getEmail());
        if(userRepository.existsByEmail(signUpRequest.getEmail())) {
            logger.warn("Registration failed: Email already in use - {}", signUpRequest.getEmail());
            throw new BadRequestException("Email address already in use.");
        }

        User user = new User(
                signUpRequest.getName(),
                signUpRequest.getEmail(),
                signUpRequest.getPassword()
        );

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        logger.info("Password encoded for user: {}", signUpRequest.getEmail());

        User result = userRepository.save(user);
        logger.info("User registered successfully with ID: {}", result.getId());

        URI location = ServletUriComponentsBuilder
                .fromCurrentContextPath().path("/user/me")
                .buildAndExpand(result.getId()).toUri();

        return ResponseEntity.created(location)
                .body(new ApiResponse(true, "User registered successfully"));
    }
}