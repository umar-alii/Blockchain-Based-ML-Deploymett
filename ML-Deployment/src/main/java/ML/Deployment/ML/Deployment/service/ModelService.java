package ML.Deployment.ML.Deployment.service;


import ML.Deployment.ML.Deployment.exception.BadRequestException;
import ML.Deployment.ML.Deployment.exception.FileUploadException;
import ML.Deployment.ML.Deployment.model.Model;
import ML.Deployment.ML.Deployment.repository.ModelRepository;
import ML.Deployment.ML.Deployment.security.UserPrincipal; // Assuming you have this
import com.fasterxml.jackson.databind.ObjectMapper; // Import ObjectMapper
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
public class ModelService {

    private static final Logger logger = LoggerFactory.getLogger(ModelService.class);

    @Autowired
    private ModelRepository modelRepository;

    @Autowired
    private HuggingFaceService huggingFaceService; // Inject the HF service

    private final ObjectMapper objectMapper = new ObjectMapper(); // For JSON validation

    public Model uploadAndDeployModel(
            UserPrincipal currentUser,
            MultipartFile file,
            String name,
            String description,
            String metadata, // Assuming metadata is also just a string for now
            String predictionParametersJson
            ) {

        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
        logger.info("Processing model upload/deploy request. User: '{}', Filename: '{}', Model Name: '{}'",
                    currentUser.getEmail(), originalFilename, name);

        // --- Basic Input Validation ---
        if (originalFilename.contains("..")) {
            throw new FileUploadException("Filename contains invalid path sequence: " + originalFilename);
        }
        if (file.isEmpty()) {
             throw new FileUploadException("Cannot process empty file: " + originalFilename);
        }
        if (file.getSize() >= 100 * 1024 * 1024) { // Check size limit (optional but good)
             logger.warn("Model file size is >= 100MB ({} bytes), deployment might fail without Git LFS.", file.getSize());
             // throw new FileUploadException("Model file exceeds 100MB limit."); // Uncomment to enforce limit strictly
        }
        if (!isValidJsonStructure(predictionParametersJson)) {
             throw new FileUploadException("Prediction Parameters is not valid JSON object or array.");
        }
         // Optional: Validate metadata format if it's expected to be JSON
         // if (metadata != null && !metadata.isBlank() && !isValidJsonStructure(metadata)) {
         //     throw new FileUploadException("Metadata provided is not valid JSON object or array.");
         // }

        try {
            // 1. Deploy to Hugging Face using the dedicated service
            logger.info("Starting deployment process to Hugging Face for model '{}'...", name);
            HuggingFaceService.DeploymentResult deploymentResult = huggingFaceService.deployModelToSpace(
                    file,
                    name,
                    currentUser.getUsername(), // Use username for potential Space naming convention
                    predictionParametersJson
            );
            logger.info("Deployment process completed. Space ID: {}, Endpoint URL: {}",
                        deploymentResult.spaceId(), deploymentResult.endpointUrl());

            // 2. Create and save Model entity to MongoDB
            Model model = new Model();
            model.setName(name);
            model.setDescription(description != null ? description : ""); // Handle potential null description
            model.setOriginalFilename(originalFilename);
            model.setSize(file.getSize());
            // model.setUploadTimestamp(Instant.now()); // Set in constructor or here
            model.setOwnerId(currentUser.getId());
            model.setOwnerUsername(currentUser.getUsername()); // Store username

            model.setMetadata(metadata); // Store provided metadata string
            model.setPredictionParametersJson(predictionParametersJson);
            model.setDeploymentPlatform("HuggingFace");
            model.setSpaceId(deploymentResult.spaceId()); // Store HF Space ID
            model.setEndpointUrl(deploymentResult.endpointUrl()); // Store HF Space URL

            Model savedModel = modelRepository.save(model);
            logger.info("Model metadata saved to MongoDB with ID: {}", savedModel.getId());
            return savedModel;

        } catch (BadRequestException | FileUploadException e) {
             logger.error("Deployment failed for model '{}': {}", name, e.getMessage());
             throw e; // Re-throw specific exceptions
        } catch (Exception e) { // Catch unexpected errors
            logger.error("Unexpected error during model upload/deployment for file: {}", originalFilename, e);
            throw new FileUploadException("Unexpected error processing file " + originalFilename + ": " + e.getMessage(), e);
        }
    }

     // Basic check if string is a valid JSON object or array
     private boolean isValidJsonStructure(String jsonString) {
         if (jsonString == null || jsonString.isBlank()) return false;
         String trimmed = jsonString.trim();
         boolean isObject = trimmed.startsWith("{") && trimmed.endsWith("}");
         boolean isArray = trimmed.startsWith("[") && trimmed.endsWith("]");
         if (!isObject && !isArray) return false;

         // Try full parsing for better validation
         try {
             objectMapper.readTree(trimmed); // Use Jackson's tree model parsing
             return true;
         } catch (IOException e) {
              logger.warn("JSON structure validation failed for input: {}", jsonString, e);
             return false;
         }
     }

    // --- Other Service Methods ---
    public List<Model> getModelsByOwner(String ownerId) {
        logger.debug("Fetching models for ownerId: {}", ownerId);
        return modelRepository.findByOwnerId(ownerId);
    }

    public Optional<Model> findById(String id) {
         logger.debug("Fetching model by ID: {}", id);
        return modelRepository.findById(id);
    }

    // TODO: Implement deleteModel - consider HF Space cleanup strategy
    // Deleting HF spaces programmatically might require different API calls or manual action.
    public void deleteModel(String modelId, UserPrincipal currentUser) {
        logger.warn("Delete operation requested for model ID: {}. Deleting from DB only.", modelId);
        Optional<Model> modelOpt = findById(modelId);
        if (modelOpt.isPresent()) {
            Model model = modelOpt.get();
            // Authorization check: Ensure current user owns the model
            if (!model.getOwnerId().equals(currentUser.getId())) {
                 logger.error("User '{}' attempted to delete model '{}' owned by '{}'",
                              currentUser.getEmail(), modelId, model.getOwnerUsername());
                 throw new SecurityException("User not authorized to delete this model.");
            }
             logger.info("Deleting model '{}' (Space ID: {}) from database.", model.getName(), model.getSpaceId());
            modelRepository.deleteById(modelId);
            logger.info("Model ID {} deleted from database.", modelId);
            // NOTE: This does NOT delete the Hugging Face Space.
        } else {
            logger.warn("Model ID {} not found for deletion.", modelId);
        }
    }
}