package ML.Deployment.ML.Deployment.controller;

import ML.Deployment.ML.Deployment.exception.BadRequestException; // For input validation
import ML.Deployment.ML.Deployment.exception.ResourceNotFoundException;
import ML.Deployment.ML.Deployment.model.Model;
import ML.Deployment.ML.Deployment.model.User;
import ML.Deployment.ML.Deployment.model.Role; // Assuming Role is in the model package
import ML.Deployment.ML.Deployment.payload.AdminUserDto;
import ML.Deployment.ML.Deployment.payload.ApiResponse;
import ML.Deployment.ML.Deployment.payload.AdminModelDto;
import ML.Deployment.ML.Deployment.repository.ModelRepository;
import ML.Deployment.ML.Deployment.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid; // Keep if needed elsewhere, removed from updateUserRoles method signature
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);

    private static final Set<String> VALID_ROLE_STRINGS = Set.of("ROLE_USER", "ROLE_ADMIN");

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ModelRepository modelRepository;


    @GetMapping("/users")
    public ResponseEntity<List<AdminUserDto>> getAllUsers() {
        logger.info("ADMIN ACCESS: Retrieving all users.");
        List<User> users = userRepository.findAll();
        List<AdminUserDto> userDtos = users.stream()
                .map(user -> mapUserToAdminDto(user, false))
                .collect(Collectors.toList());
        return ResponseEntity.ok(userDtos);
    }



    @GetMapping("/users/{userId}")
    public ResponseEntity<AdminUserDto> getUserById(@PathVariable String userId) {
        logger.info("ADMIN ACCESS: Retrieving user by ID: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        AdminUserDto userDto = mapUserToAdminDto(user, true);
        return ResponseEntity.ok(userDto);
    }


    @PutMapping("/users/{userId}/roles")
    public ResponseEntity<ApiResponse> updateUserRoles(@PathVariable String userId, @RequestBody Set<String> newRoleStrings) {
        logger.info("ADMIN ACTION: Attempting to update roles for user ID: {} to {}", userId, newRoleStrings);


        if (newRoleStrings == null || newRoleStrings.isEmpty()) {
            throw new BadRequestException("Roles cannot be empty.");
        }
        for (String roleStr : newRoleStrings) {
            if (!VALID_ROLE_STRINGS.contains(roleStr)) {
                throw new BadRequestException("Invalid role provided: " + roleStr + ". Valid roles are: " + VALID_ROLE_STRINGS);
            }
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));


        try {
            Set<Role> newRoles = newRoleStrings.stream()
                    .map(Role::valueOf)
                    .collect(Collectors.toSet());
            user.setRoles(newRoles);
        } catch (IllegalArgumentException e) {

            throw new BadRequestException("Invalid role value provided in the set.", e);
        }


        userRepository.save(user);

        logger.info("ADMIN ACTION: Successfully updated roles for user ID: {}", userId);
        return ResponseEntity.ok(new ApiResponse(true, "User roles updated successfully."));
    }


    @DeleteMapping("/users/{userId}")
    public ResponseEntity<ApiResponse> deleteUser(@PathVariable String userId) {
        logger.warn("ADMIN ACTION: Attempting to permanently delete user ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));


        List<Model> userModels = modelRepository.findByOwnerId(userId);
        if (!userModels.isEmpty()) {
            logger.warn("ADMIN ACTION: Deleting {} models associated with user ID: {}", userModels.size(), userId);

            modelRepository.deleteAll(userModels);
        }


        userRepository.deleteById(userId);

        logger.info("ADMIN ACTION: Successfully deleted user ID: {} and their associated models.", userId);
        return ResponseEntity.ok(new ApiResponse(true, "User and associated models deleted successfully."));
    }



    @GetMapping("/models")
    public ResponseEntity<List<AdminModelDto>> getAllModels() {
        logger.info("ADMIN ACCESS: Retrieving all models.");
        List<Model> models = modelRepository.findAll();
        List<AdminModelDto> modelDtos = models.stream()
                .map(this::mapModelToAdminDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(modelDtos);
    }


    @GetMapping("/models/{modelId}")
    public ResponseEntity<AdminModelDto> getModelById(@PathVariable String modelId) {
        logger.info("ADMIN ACCESS: Retrieving model by ID: {}", modelId);
        Model model = modelRepository.findById(modelId)
                .orElseThrow(() -> new ResourceNotFoundException("Model", "id", modelId));
        AdminModelDto modelDto = mapModelToAdminDto(model);
        return ResponseEntity.ok(modelDto);
    }


    @PatchMapping("/models/{modelId}/make-private")
    public ResponseEntity<ApiResponse> makeModelPrivate(@PathVariable String modelId) {
        logger.info("ADMIN ACTION: Making model private: {}", modelId);
        Model model = modelRepository.findById(modelId)
                .orElseThrow(() -> new ResourceNotFoundException("Model", "id", modelId));

        if (!model.isPublic()) {
            logger.warn("ADMIN ACTION: Model {} is already private.", modelId);
            return ResponseEntity.ok(new ApiResponse(true, "Model is already private."));
        }

        model.setPublic(false);
        modelRepository.save(model);
        logger.info("ADMIN ACTION: Model {} successfully set to private.", modelId);
        return ResponseEntity.ok(new ApiResponse(true, "Model successfully set to private."));
    }


    @PatchMapping("/models/{modelId}/make-public")
    public ResponseEntity<ApiResponse> makeModelPublic(@PathVariable String modelId) {
        logger.info("ADMIN ACTION: Making model public: {}", modelId);
        Model model = modelRepository.findById(modelId)
                .orElseThrow(() -> new ResourceNotFoundException("Model", "id", modelId));

        if (model.isPublic()) {
            logger.warn("ADMIN ACTION: Model {} is already public.", modelId);
            return ResponseEntity.ok(new ApiResponse(true, "Model is already public."));
        }

        model.setPublic(true);
        modelRepository.save(model);
        logger.info("ADMIN ACTION: Model {} successfully set to public.", modelId);
        return ResponseEntity.ok(new ApiResponse(true, "Model successfully set to public."));
    }


    @DeleteMapping("/models/{modelId}")
    public ResponseEntity<ApiResponse> deleteModel(@PathVariable String modelId) {
        logger.warn("ADMIN ACTION: Attempting to permanently delete model ID: {}", modelId);


        if (!modelRepository.existsById(modelId)) {
            throw new ResourceNotFoundException("Model", "id", modelId);
        }


        modelRepository.deleteById(modelId);

        logger.info("ADMIN ACTION: Successfully deleted model ID: {}", modelId);
        return ResponseEntity.ok(new ApiResponse(true, "Model deleted successfully."));
    }



    private AdminUserDto mapUserToAdminDto(User user, boolean includeModels) {
        AdminUserDto dto = new AdminUserDto();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());


        if (user.getRoles() != null) {
            Set<String> roleNames = user.getRoles().stream()

                    .map(Role::name)
                    .collect(Collectors.toSet());
            dto.setRoles(roleNames);
        } else {
            dto.setRoles(Set.of());
        }

        if (includeModels) {
            List<Model> userModels = modelRepository.findByOwnerId(user.getId());
            dto.setModelCount(userModels.size());
            dto.setModels(userModels.stream().map(this::mapModelToAdminDto).collect(Collectors.toList()));
        } else {

            dto.setModelCount(modelRepository.countByOwnerId(user.getId()));
            dto.setModels(null);
        }
        return dto;
    }


    private AdminModelDto mapModelToAdminDto(Model model) {
        AdminModelDto dto = new AdminModelDto();
        dto.setId(model.getId());
        dto.setName(model.getName());
        dto.setDescription(model.getDescription());
        dto.setFilename(model.getFilename());
        dto.setContentType(model.getContentType());
        dto.setSize(model.getSize());
        dto.setUploadTimestamp(model.getUploadTimestamp());
        dto.setPublic(model.isPublic());
        dto.setOwnerId(model.getOwnerId());


        if (model.getOwnerId() != null) {
            userRepository.findById(model.getOwnerId())
                    .ifPresent(owner -> dto.setOwnerEmail(owner.getEmail()));
        }

        return dto;
    }
}