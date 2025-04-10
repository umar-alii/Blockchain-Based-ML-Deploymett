package ML.Deployment.ML.Deployment.controller;

import ML.Deployment.ML.Deployment.exception.ResourceNotFoundException;
import ML.Deployment.ML.Deployment.model.User;
import ML.Deployment.ML.Deployment.payload.ApiResponse;
import ML.Deployment.ML.Deployment.payload.EmailChangeRequest;
import ML.Deployment.ML.Deployment.payload.PasswordChangeRequest;
import ML.Deployment.ML.Deployment.payload.ProfileUpdateRequest;
import ML.Deployment.ML.Deployment.repository.UserRepository;
import ML.Deployment.ML.Deployment.security.CurrentUser;
import ML.Deployment.ML.Deployment.security.UserPrincipal;
import ML.Deployment.ML.Deployment.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public User getCurrentUser(@CurrentUser UserPrincipal userPrincipal) {

        return userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userPrincipal.getId()));
    }

    @PutMapping("/me/profile")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<User> updateProfile(
            @CurrentUser UserPrincipal currentUser,
            @Valid @RequestBody ProfileUpdateRequest profileUpdateRequest
    ) {
        User updatedUser = userService.updateProfile(currentUser, profileUpdateRequest);
        return ResponseEntity.ok(updatedUser);
    }

    @PostMapping("/me/password")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse> changePassword(
            @CurrentUser UserPrincipal currentUser,
            @Valid @RequestBody PasswordChangeRequest passwordChangeRequest
    ) {
        userService.changePassword(currentUser, passwordChangeRequest);
        return ResponseEntity.ok(new ApiResponse(true, "Password changed successfully"));
    }

    @PostMapping("/me/email")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> changeEmail(
                                          @CurrentUser UserPrincipal currentUser,
                                          @Valid @RequestBody EmailChangeRequest emailChangeRequest
    ) {

        userService.changeEmail(currentUser, emailChangeRequest);
        return ResponseEntity.ok(new ApiResponse(true, "Email change request successful. Please use the new email to log in."));

    }

    @GetMapping("/me/profile-status")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse> getProfileStatus(@CurrentUser UserPrincipal currentUser) {
        boolean isComplete = userService.isProfileComplete(currentUser);
        String message = isComplete ? "Profile is complete." : "Profile requires completion.";
        return ResponseEntity.ok(new ApiResponse(isComplete, message));
    }
}