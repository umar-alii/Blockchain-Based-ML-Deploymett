package ML.Deployment.ML.Deployment.service;

import ML.Deployment.ML.Deployment.exception.DeploymentException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class HuggingFaceService {

    private static final Logger logger = LoggerFactory.getLogger(HuggingFaceService.class);

    @Value("${huggingface.token}")
    private String hfToken;

    @Value("${huggingface.username}")
    private String hfUsername;

    @Value("${temp.file.dir}")
    private String tempFileDir;

    // Optional: Path to git executable if not in system PATH
    // @Value("${git.executable.path:git}") // Defaults to "git"
    // private String gitExecutable;
    private final String gitExecutable = "git"; // Assuming git is in PATH

    private final ObjectMapper objectMapper = new ObjectMapper();

    public record DeploymentResult(String spaceId, String endpointUrl) {}

    public DeploymentResult deployModelToSpace(MultipartFile modelFile, String modelName, String ownerUsername, String predictionParametersJson) {
        Path tempDir = null;
        Path modelFilePath = null;
        String spaceName = generateSpaceName(ownerUsername, modelName);
        String spaceId = hfUsername + "/" + spaceName; // Full HF Space ID
        // *** SECURITY WARNING: Embedding token in URL is risky! ***
        String remoteUrl = String.format("https://%s:%s@huggingface.co/spaces/%s", hfUsername, hfToken, spaceId);
        String finalSpaceUrl = String.format("https://huggingface.co/spaces/%s", spaceId);

        try {
            // 1. Create temporary directory
            tempDir = Files.createDirectories(Paths.get(tempFileDir, UUID.randomUUID().toString()));
            logger.info("Created temporary directory for deployment: {}", tempDir);

            // 2. Save model file temporarily
            String originalModelFilename = modelFile.getOriginalFilename() != null ? modelFile.getOriginalFilename() : "model.pkl";
            modelFilePath = tempDir.resolve(originalModelFilename);
            Files.copy(modelFile.getInputStream(), modelFilePath, StandardCopyOption.REPLACE_EXISTING);
            logger.info("Saved model file temporarily to: {}", modelFilePath);

            // 3. Generate necessary Python/Docker/Readme files in the temp directory
            generateRequiredFiles(tempDir, modelName, predictionParametersJson, originalModelFilename);
            logger.info("Generated deployment files (main.py, Dockerfile, etc.) in {}", tempDir);

            // --- 4. Execute Git Commands ---
            logger.info("Initializing Git repository in {}", tempDir);
            executeGitCommand(tempDir, gitExecutable, "init", "--initial-branch=main"); // Ensure main branch

            logger.info("Adding remote origin: {}", remoteUrl.replace(":"+hfToken+"@", ":<TOKEN>@")); // Log URL without token
            executeGitCommand(tempDir, gitExecutable, "remote", "add", "origin", remoteUrl);

            logger.info("Adding all files to Git index");
            executeGitCommand(tempDir, gitExecutable, "add", ".");

            logger.info("Committing files");
            executeGitCommand(tempDir, gitExecutable, "commit", "-m", "Deploy model and application code via automated service");

            logger.info("Pushing to Hugging Face Space repository (origin main)");
            executeGitCommand(tempDir, gitExecutable, "push", "origin", "main", "--force"); // Force push for overwrite

            // --- End Git Commands ---

            logger.info("Successfully pushed files to Hugging Face Space repository.");
            logger.info("Deployment presumed successful. Space ID: {}, Endpoint URL: {}", spaceId, finalSpaceUrl);

            return new DeploymentResult(spaceId, finalSpaceUrl);

        } catch (IOException | InterruptedException e) {
            logger.error("Error during Hugging Face deployment process using Git", e);
            // Clean up thread interruption status if needed
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            throw new DeploymentException("Deployment failed using Git: " + e.getMessage(), e);
        } finally {
            // 6. Clean up temporary directory
            if (tempDir != null && Files.exists(tempDir)) {
                logger.info("Attempting to clean up temporary directory: {}", tempDir);
                try {
                    // Use Java NIO for potentially more robust deletion
                    Files.walk(tempDir)
                         .sorted(Comparator.reverseOrder())
                         .map(Path::toFile)
                         // .peek(f -> logger.debug("Deleting: " + f.getPath())) // Uncomment for debug
                         .forEach(File::delete);
                    if (!Files.exists(tempDir)) {
                         logger.info("Successfully cleaned up temporary directory: {}", tempDir);
                    } else {
                         logger.warn("Temporary directory might not be fully cleaned up: {}", tempDir);
                    }
                } catch (IOException e) {
                    logger.error("Failed to clean up temporary directory: {}", tempDir, e);
                }
            }
        }
    }

    private void executeGitCommand(Path workingDir, String... command) throws IOException, InterruptedException, DeploymentException {
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.directory(workingDir.toFile());
        pb.redirectErrorStream(true);

        logger.debug("Executing command: {} in {}", String.join(" ", command), workingDir);
        Process process = pb.start();

        StringBuilder output = new StringBuilder();
        // Try-with-resources ensures the reader is closed
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append(System.lineSeparator());
            }
        }

        // Wait for the process to complete (add a reasonable timeout)
        boolean finished = process.waitFor(3, TimeUnit.MINUTES); // 3-minute timeout
        int exitCode = -1; // Default value
        if (finished) {
             exitCode = process.exitValue();
        } else {
             process.destroyForcibly(); // Kill if it times out
             logger.error("Command timed out after 3 minutes: {}", String.join(" ", command));
             throw new DeploymentException("Command timed out: " + String.join(" ", command));
        }


        logger.debug("Command finished. Exit Code: {}. Output:\n---\n{}---", exitCode, output);

        if (exitCode != 0) {
            throw new DeploymentException("Command failed with exit code " + exitCode + ": " + String.join(" ", command) + "\nOutput:\n" + output);
        }
         // Check for specific git error messages in output if needed
         if (output.toString().toLowerCase().contains("error:") || output.toString().toLowerCase().contains("fatal:")) {
              logger.warn("Potential error detected in git command output even with exit code 0 for command: {}", String.join(" ", command));
              // Decide if this should be a hard failure
              // throw new DeploymentException("Git command reported errors despite exit code 0: " + String.join(" ", command) + "\nOutput:\n" + output);
         }
    }

    private String generateSpaceName(String ownerUsername, String modelName) {
        String safeModelName = modelName.toLowerCase()
                                        .replaceAll("[^a-z0-9\\-]+", "") // Allow letters, numbers, hyphen
                                        .replaceAll("\\s+", "-") // Replace spaces with hyphens
                                        .replaceAll("-{2,}", "-") // Collapse multiple hyphens
                                        .replaceAll("^-|-$", ""); // Trim leading/trailing hyphens
        if (safeModelName.length() > 40) { // Keep length reasonable
             safeModelName = safeModelName.substring(0, 40);
        }
        if (safeModelName.isEmpty()) {
             safeModelName = "model";
        }
        // Using a shorter UUID part
        return "ml-" + safeModelName + "-" + UUID.randomUUID().toString().substring(0, 6);
    }

    private void generateRequiredFiles(Path targetDir, String modelName, String predictionParametersJson, String modelFilenameInRepo) throws IOException {
        // Generate requirements.txt
        String requirements = """
                fastapi>=0.70.0,<0.100.0
                uvicorn[standard]>=0.15.0,<0.21.0
                scikit-learn>=1.0.0,<1.4.0
                joblib>=1.0.0,<1.3.0
                numpy>=1.21.0,<1.25.0
                python-multipart>=0.0.5,<0.0.7
                pydantic>=1.9.0,<2.0.0
                """.stripIndent(); // Pin versions for better reproducibility
        Files.writeString(targetDir.resolve("requirements.txt"), requirements, StandardCharsets.UTF_8);

        // Generate Dockerfile
        // Use fixed name 'model.pkl' inside container for simplicity in main.py
        String dockerfile = """
                FROM python:3.9-slim
                WORKDIR /code
                COPY ./requirements.txt /code/requirements.txt
                RUN pip install --no-cache-dir --upgrade -r /code/requirements.txt
                COPY ./main.py /code/main.py
                COPY ./%s /code/model.pkl
                EXPOSE 7860
                CMD ["uvicorn", "main:app", "--host", "0.0.0.0", "--port", "7860"]
                """.formatted(modelFilenameInRepo).stripIndent();
        Files.writeString(targetDir.resolve("Dockerfile"), dockerfile, StandardCharsets.UTF_8);

        // Generate README.md
        String readme = """
                ---
                title: %s Prediction API
                emoji: 🚀
                colorFrom: indigo
                colorTo: green
                sdk: docker
                app_port: 7860
                # pinned: false # Optional
                ---

                # %s Prediction API

                Deployed via automated service using Git. Use the `/predict` endpoint.

                **Input JSON structure required:** (Defined by user during upload)
                Refer to the application database or API documentation for the exact fields needed by this specific model deployment.

                *(Current Date: %s)*
                """.formatted(modelName, modelName, java.time.LocalDate.now()).stripIndent();
        Files.writeString(targetDir.resolve("README.md"), readme, StandardCharsets.UTF_8);

        // Generate main.py (Dynamically based on predictionParametersJson)
        String mainPyContent = generateMainPyContent(predictionParametersJson, "model.pkl"); // Use fixed name inside container
        Files.writeString(targetDir.resolve("main.py"), mainPyContent, StandardCharsets.UTF_8);
    }


    private String generateMainPyContent(String predictionParametersJson, String modelPathInContainer) throws JsonProcessingException {
         List<String> fieldNames;
         String pydanticFields = "";
         String featureArrayAssignment = "";
         String inputDataLogging = ""; // For logging input fields

         try {
             // Try parsing as Map first (allows specifying types or aliases if needed later)
             Map<String, Object> paramMap = objectMapper.readValue(predictionParametersJson, new TypeReference<Map<String, Object>>() {});
             fieldNames = paramMap.keySet().stream().collect(Collectors.toList());

             StringBuilder pydanticBuilder = new StringBuilder();
             StringBuilder featureBuilder = new StringBuilder("np.array([[\n");
             StringBuilder logBuilder = new StringBuilder("{\n");

             for (String field : fieldNames) {
                 // Basic type mapping - default to float for simplicity
                 String pydanticType = "float";
                 // Simple alias generation for camelCase etc.
                 String alias = field.contains("_") ? field.replaceAll("_([a-z])", "$1".toUpperCase()) : field;
                 // Ensure field name is valid Python identifier
                 String validPythonField = field.replaceAll("[^a-zA-Z0-9_]", "_");

                 pydanticBuilder.append(String.format("    %s: %s = Field(..., alias=\"%s\")\n", validPythonField, pydanticType, alias));
                 featureBuilder.append(String.format("            input_data.%s,\n", validPythonField));
                 logBuilder.append(String.format("        f'    \"%s\": {{input_data.%s}},\\n'\n", alias, validPythonField));
             }
             pydanticFields = pydanticBuilder.toString();
             featureArrayAssignment = featureBuilder.append("        ]])").toString();
             inputDataLogging = logBuilder.append("        '    }'\n    )").toString();


         } catch (Exception e) {
             logger.warn("Could not parse predictionParametersJson as Map, trying as List. Error: {}", e.getMessage());
             // Fallback: Try parsing as simple List<String>
             try {
                 fieldNames = objectMapper.readValue(predictionParametersJson, new TypeReference<List<String>>() {});
                 StringBuilder pydanticBuilder = new StringBuilder();
                 StringBuilder featureBuilder = new StringBuilder("np.array([[\n");
                 StringBuilder logBuilder = new StringBuilder("{\n");

                 for (String field : fieldNames) {
                     String validPythonField = field.replaceAll("[^a-zA-Z0-9_]", "_");
                     pydanticBuilder.append(String.format("    %s: float\n", validPythonField)); // Assume float, no alias
                     featureBuilder.append(String.format("            input_data.%s,\n", validPythonField));
                     logBuilder.append(String.format("        f'    \"%s\": {{input_data.%s}},\\n'\n", field, validPythonField));
                 }
                 pydanticFields = pydanticBuilder.toString();
                 featureArrayAssignment = featureBuilder.append("        ]])").toString();
                  inputDataLogging = logBuilder.append("        '    }'\n    )").toString();

             } catch (Exception e2) {
                 logger.error("Failed to parse predictionParametersJson as Map or List. Using default empty structure.", e2);
                 throw new DeploymentException("Invalid format for Prediction Parameters JSON. Expected JSON object (like {\"field\": \"type\"}) or list of strings (like [\"field1\", \"field2\"]).", e2);
             }
         }


        // Template for main.py
        return """
import os
import sys
import joblib
import numpy as np
from fastapi import FastAPI, HTTPException, Request
from fastapi.responses import JSONResponse
from pydantic import BaseModel, Field, ValidationError
import logging
import time

# --- Configuration & Setup ---
MODEL_FILENAME = "%s" # Model filename inside container
MODEL_PATH = os.path.join(os.path.dirname(__file__), MODEL_FILENAME)

logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(levelname)s - %(name)s - %(message)s',
    handlers=[logging.StreamHandler(sys.stdout)]
)
logger = logging.getLogger(__name__)

# --- Load Model ---
model = None
startup_error = None
try:
    start_time = time.time()
    logger.info(f"Attempting to load model from: {MODEL_PATH}")
    if not os.path.exists(MODEL_PATH):
        startup_error = f"Model file not found at expected path: {MODEL_PATH}"
        logger.error(startup_error)
    else:
        model = joblib.load(MODEL_PATH)
        load_time = time.time() - start_time
        logger.info(f"Model loaded successfully from {MODEL_PATH} in {load_time:.2f} seconds.")
except Exception as e:
    startup_error = f"Failed to load the model from {MODEL_PATH}: {e}"
    logger.exception(startup_error) # Log full traceback during startup

# --- Pydantic Models (Dynamically generated) ---
class PredictionInput(BaseModel):
%s
    class Config:
        allow_population_by_field_name = True # Allow using aliases if defined

class PredictionOutput(BaseModel):
    prediction: float # Assuming float output - adjust if model output type differs

# --- FastAPI Application ---
app = FastAPI(
    title="ML Model Prediction API",
    description="Dynamically generated API for ML model prediction.",
    version="1.1.0"
)

# --- Middleware for Logging and Error Handling ---
@app.middleware("http")
async def log_requests(request: Request, call_next):
    start_time = time.time()
    log_dict = {
        "method": request.method,
        "url": str(request.url),
        "client_host": request.client.host,
        "client_port": request.client.port,
    }
    logger.info(f"Request started: {log_dict}")
    try:
        response = await call_next(request)
        process_time = time.time() - start_time
        response.headers["X-Process-Time"] = str(process_time)
        log_dict["status_code"] = response.status_code
        log_dict["process_time_seconds"] = f"{process_time:.4f}"
        logger.info(f"Request finished: {log_dict}")
        return response
    except Exception as e:
        process_time = time.time() - start_time
        logger.exception(f"Request failed after {process_time:.4f}s: {log_dict}")
        return JSONResponse(status_code=500, content={"detail": "Internal server error during request processing."})

# --- API Endpoints ---
@app.get("/", summary="Root endpoint", tags=["General"])
async def root():
    """ Basic endpoint to check if the API is running. """
    logger.info("Root endpoint '/' accessed.")
    return {"message": "Prediction API is running. Use POST /predict for predictions."}

@app.get("/health", summary="Health Check", tags=["General"])
async def health_check():
    """ Returns the operational status of the API and model loading status. """
    if startup_error:
        logger.error(f"Health check failed due to startup error: {startup_error}")
        raise HTTPException(status_code=503, detail=f"Service Unavailable: {startup_error}")
    status = {"status": "ok", "model_loaded": model is not None}
    logger.info(f"Health check successful: {status}")
    return status

@app.post("/predict", response_model=PredictionOutput, summary="Make Prediction", tags=["Prediction"])
async def predict(input_data: PredictionInput):
    """
    Receives input data matching the model's requirements and returns the prediction.
    """
    # Check if model loaded correctly during startup
    if model is None or startup_error:
        error_detail = startup_error or "Model is not available."
        logger.error(f"Prediction attempt failed: {error_detail}")
        raise HTTPException(status_code=503, detail=f"Service Unavailable: {error_detail}")

    # Log the received input data (using dynamically generated f-string)
    log_message = f"Prediction request received with input:\\n{{\\n" + (
%s # Dynamically generated logging string
    )
    logger.info(log_message)


    try:
        # Prepare features - ASSUMES numpy array input based on order in JSON
        # (Dynamically generated based on parsed fields)
        features = %s
        logger.debug(f"Features prepared for model prediction: {features.tolist()}") # Log numpy array content

        # Make Prediction
        start_pred_time = time.time()
        prediction_result = model.predict(features)
        pred_time = time.time() - start_pred_time
        logger.debug(f"Raw prediction result: {prediction_result} (obtained in {pred_time:.4f}s)")

        # Extract and process the prediction result
        # Adjust based on expected output shape/type (e.g., regression, classification)
        output_value = prediction_result[0] if isinstance(prediction_result, (np.ndarray, list)) and len(prediction_result) > 0 else prediction_result

        # Convert numpy types to standard Python types for JSON serialization
        if isinstance(output_value, np.generic):
            output_value = output_value.item()

        logger.info(f"Prediction successful. Output value: {output_value}")
        return PredictionOutput(prediction=output_value)

    except ValidationError as ve:
         # Should be caught by FastAPI, but handle explicitly just in case
         logger.error(f"Input validation error: {ve.errors()}", exc_info=True)
         raise HTTPException(status_code=422, detail={"message": "Input validation failed", "errors": ve.errors()})
    except Exception as e:
        logger.exception("An unexpected error occurred during prediction.") # Log full traceback
        raise HTTPException(status_code=500, detail=f"Prediction failed due to an internal error: {type(e).__name__}")

# Optional: Add exception handler for validation errors if needed globally
# @app.exception_handler(ValidationError)
# async def validation_exception_handler(request: Request, exc: ValidationError):
#     return JSONResponse(
#         status_code=422,
#         content={"detail": "Validation Error", "errors": exc.errors()},
#     )

# Note: Uvicorn runs this app using the CMD in the Dockerfile
""".formatted(
                        modelPathInContainer,      # %s for MODEL_PATH
                        pydanticFields,            # %s for Pydantic fields in PredictionInput
                        inputDataLogging,          # %s for dynamic logging f-string
                        featureArrayAssignment     # %s for feature preparation np.array
                ).stripIndent();
    }

} // End of HuggingFaceService class