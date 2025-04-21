package ML.Deployment.ML.Deployment.controller;

import ML.Deployment.ML.Deployment.exception.DeploymentException;
import ML.Deployment.ML.Deployment.exception.FileUploadException;
import ML.Deployment.ML.Deployment.model.Model;
import ML.Deployment.ML.Deployment.payload.ApiResponse;
import ML.Deployment.ML.Deployment.payload.ModelDto;
import ML.Deployment.ML.Deployment.security.CurrentUser;
import ML.Deployment.ML.Deployment.security.UserPrincipal; // Assuming you have this
import ML.Deployment.ML.Deployment.service.ModelService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException; // For cleaner error responses

import jakarta.validation.constraints.NotBlank; // Using jakarta validation
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/models")
public class ModelController {

    private static final Logger logger = LoggerFactory.getLogger(ModelController.class);

    @Autowired
    private ModelService modelService;

    /**
     * Endpoint to upload a model file and associated metadata,
     * triggering deployment to Hugging Face using Git commands.
     */
    @PostMapping("/upload-hf-git") // Changed endpoint name for clarity
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> uploadAndDeployWithGit(
            @CurrentUser UserPrincipal currentUser,
            @RequestParam("name") @NotBlank(message = "Model name cannot be blank") String name,
            @RequestParam(value = "description", required = false) String description, // Optional
            @RequestParam("metadata") @NotBlank(message = "Metadata cannot be blank") String metadata,
            @RequestParam("predictionParametersJson") @NotBlank(message = "Prediction parameters JSON cannot be blank") String predictionParametersJson,
            @RequestParam("file") @NotNull(message = "Model file must be provided") MultipartFile file
            ) {

        logger.info("Received deploy-to-HF-via-Git request. User: '{}', Model Name: '{}', Filename: '{}'",
                    currentUser.getEmail(), name, file.getOriginalFilename());
        try {
            Model savedModel = modelService.uploadAndDeployModel(
                    currentUser, file, name, description, metadata, predictionParametersJson
            );
            ModelDto responseDto = mapModelToDto(savedModel);
            logger.info("Successfully deployed model '{}' via Git. DB ID: {}, HF Space: {}",
                        name, savedModel.getId(), savedModel.getSpaceId());
            return ResponseEntity.status(HttpStatus.CREATED).body(responseDto); // Return 201 Created

        } catch (FileUploadException | DeploymentException | IllegalArgumentException e) {
            logger.warn("Failed to upload/deploy model '{}' via Git: {}", name, e.getMessage());
            // Return 4xx for client-side errors (bad input, invalid format)
            HttpStatus status = (e instanceof IllegalArgumentException || e instanceof FileUploadException) ?
                                HttpStatus.BAD_REQUEST : HttpStatus.INTERNAL_SERVER_ERROR;
            throw new ResponseStatusException(status, "Deployment failed: " + e.getMessage(), e);
        } catch (Exception e) { // Catch unexpected errors
            logger.error("Unexpected error during deploy-to-HF-via-Git for model '{}': {}", name, e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred during deployment.", e);
        }
    }

    /**
     * Retrieves models owned by the currently authenticated user.
     */
     @GetMapping("/my-models")
     @PreAuthorize("isAuthenticated()")
     public List<ModelDto> getCurrentUserModels(@CurrentUser UserPrincipal currentUser) {
         logger.debug("Fetching models for user: {}", currentUser.getEmail());
         return modelService.getModelsByOwner(currentUser.getId())
                 .stream()
                 .map(this::mapModelToDto)
                 .collect(Collectors.toList());
     }

    /**
     * Retrieves details for a specific model by its ID.
     * Ensures the requesting user owns the model.
     */
     @GetMapping("/{modelId}")
     @PreAuthorize("isAuthenticated()")
     public ResponseEntity<ModelDto> getModelDetails(@PathVariable String modelId, @CurrentUser UserPrincipal currentUser) {
         logger.debug("Fetching details for model ID: {} for user: {}", modelId, currentUser.getEmail());
         return modelService.findById(modelId)
                 .map(model -> {
                     // Authorization Check: Only owner can view (adjust if public models needed)
                     if (!model.getOwnerId().equals(currentUser.getId())) {
                          logger.warn("Unauthorized attempt by user '{}' to access model ID '{}'", currentUser.getEmail(), modelId);
                          throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not authorized to view this model.");
                     }
                     return ResponseEntity.ok(mapModelToDto(model));
                 })
                 .orElseThrow(() -> {
                      logger.warn("Model ID '{}' not found.", modelId);
                      return new ResponseStatusException(HttpStatus.NOT_FOUND, "Model not found with ID: " + modelId);
                 });
     }


    /**
     * Deletes a model entry from the database.
     * NOTE: This does NOT delete the associated Hugging Face Space.
     */
     @DeleteMapping("/{modelId}")
     @PreAuthorize("isAuthenticated()")
     public ResponseEntity<ApiResponse> deleteModel(@PathVariable String modelId, @CurrentUser UserPrincipal currentUser) {
         logger.info("Received delete request for model ID: {} from user: {}", modelId, currentUser.getEmail());
         try {
             modelService.deleteModel(modelId, currentUser);
             return ResponseEntity.ok(new ApiResponse(true, "Model metadata deleted successfully from database."));
         } catch (SecurityException e) {
             logger.warn("Unauthorized delete attempt for model ID {}: {}", modelId, e.getMessage());
             throw new ResponseStatusException(HttpStatus.FORBIDDEN, e.getMessage(), e);
         } catch (Exception e) {
             logger.error("Error deleting model ID {}: {}", modelId, e.getMessage(), e);
             throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to delete model metadata: " + e.getMessage(), e);
         }
     }

    // --- Helper DTO Mapping Function ---
    private ModelDto mapModelToDto(Model model) {
         ModelDto dto = new ModelDto();
         dto.setId(model.getId());
         dto.setName(model.getName());
         dto.setDescription(model.getDescription());
         dto.setOriginalFilename(model.getOriginalFilename());
         dto.setSize(model.getSize());
         dto.setUploadTimestamp(model.getUploadTimestamp());
         dto.setOwnerId(model.getOwnerId());
         dto.setOwnerUsername(model.getOwnerUsername());
         dto.setDeploymentPlatform(model.getDeploymentPlatform());
         dto.setMetadata(model.getMetadata());
         dto.setPredictionParametersJson(model.getPredictionParametersJson());
         dto.setEndpointUrl(model.getEndpointUrl());
         dto.setSpaceId(model.getSpaceId());
         return dto;
     }
}