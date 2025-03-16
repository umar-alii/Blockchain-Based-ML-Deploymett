package ML.Deployment.ML.Deployment.service;


import ML.Deployment.ML.Deployment.exception.BadRequestException;
import ML.Deployment.ML.Deployment.exception.ResourceNotFoundException;
import ML.Deployment.ML.Deployment.model.User;
import ML.Deployment.ML.Deployment.payload.EmailChangeRequest;
import ML.Deployment.ML.Deployment.payload.PasswordChangeRequest;
import ML.Deployment.ML.Deployment.payload.ProfileUpdateRequest;
import ML.Deployment.ML.Deployment.repository.UserRepository;
import ML.Deployment.ML.Deployment.security.UserPrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Transactional
    public User updateProfile(UserPrincipal currentUser, ProfileUpdateRequest request) {
        User user = findUserById(currentUser.getId());

        user.setName(request.getName());
        // Update other fields from request if added (e.g., user.setImageUrl(request.getImageUrl());)

        // Mark profile as complete if not already
        if (!user.isProfileComplete()) {
            // Add checks here if certain fields *must* be non-null/non-empty for completion
            if (StringUtils.hasText(user.getName())) { // Example check
                user.setProfileComplete(true);
                logger.info("User profile marked as complete for user ID: {}", user.getId());
            } else {
                // This shouldn't happen if @NotBlank is enforced on request, but good practice
                throw new BadRequestException("Cannot mark profile as complete without required fields (e.g., name).");
            }
        }

        return userRepository.save(user);
    }

    @Transactional
    public void changePassword(UserPrincipal currentUser, PasswordChangeRequest request) {
        User user = findUserById(currentUser.getId());

        // Verify current password
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new BadRequestException("Incorrect current password.");
        }

        // Encode and set new password
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        logger.info("Password changed successfully for user ID: {}", user.getId());
    }

    @Transactional
    public User changeEmail(UserPrincipal currentUser, EmailChangeRequest request) {
        User user = findUserById(currentUser.getId());

        // Verify password before changing sensitive info
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new BadRequestException("Incorrect password.");
        }

        // Check if the new email is already taken
        if (userRepository.existsByEmail(request.getNewEmail())) {
            throw new BadRequestException("Email address " + request.getNewEmail() + " is already in use.");
        }

        // Update email
        user.setEmail(request.getNewEmail());
        // Consider if email change should require re-verification
        // user.setEmailVerified(false); // Optional: force re-verification

        User updatedUser = userRepository.save(user);
        logger.info("Email changed successfully for user ID: {} to {}", user.getId(), request.getNewEmail());
        // Note: If email is used for login, the user needs to use the new email now.
        // Also, the JWT subject is the user ID, so existing tokens remain valid.
        return updatedUser;
    }


    // Helper to get user or throw exception
    private User findUserById(String userId) {
        return userRepository.findById(userId).orElseThrow(
                () -> new ResourceNotFoundException("User", "id", userId) // Should not happen for authenticated user
        );
    }

    // Method to check profile completion status (can be called by other services/controllers)
    public boolean isProfileComplete(UserPrincipal currentUser) {
        User user = findUserById(currentUser.getId());
        return user.isProfileComplete();
    }
}