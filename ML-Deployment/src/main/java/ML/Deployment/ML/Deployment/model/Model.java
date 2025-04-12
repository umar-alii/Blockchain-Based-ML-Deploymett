
package ML.Deployment.ML.Deployment.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.Instant;

@Document(collection = "models")
public class Model {

    @Id
    private String id;
    private String name;
    private String description;
    private String originalFilename;
    private long size;
    private Instant uploadTimestamp;
    private String ownerId;
    private String ownerUsername;

    // Deployment specific fields
    private String deploymentPlatform = "HuggingFace"; // Track where it's deployed
    private String metadata; // General information (system info, etc.) - String or structured JSON String
    private String predictionParametersJson; // JSON string defining required input parameters
    private String endpointUrl; // URL provided by Hugging Face Space
    private String spaceId; // Hugging Face Space ID (e.g., username/spacename)


    // --- Constructors ---
    public Model() {
        this.uploadTimestamp = Instant.now();
    }

    // --- Getters and Setters ---
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getOriginalFilename() { return originalFilename; }
    public void setOriginalFilename(String originalFilename) { this.originalFilename = originalFilename; }
    public long getSize() { return size; }
    public void setSize(long size) { this.size = size; }
    public Instant getUploadTimestamp() { return uploadTimestamp; }
    public void setUploadTimestamp(Instant uploadTimestamp) { this.uploadTimestamp = uploadTimestamp; }
    public String getOwnerId() { return ownerId; }
    public void setOwnerId(String ownerId) { this.ownerId = ownerId; }
    public String getOwnerUsername() { return ownerUsername; }
    public void setOwnerUsername(String ownerUsername) { this.ownerUsername = ownerUsername; }
    public String getDeploymentPlatform() { return deploymentPlatform; }
    public void setDeploymentPlatform(String deploymentPlatform) { this.deploymentPlatform = deploymentPlatform; }
    public String getMetadata() { return metadata; }
    public void setMetadata(String metadata) { this.metadata = metadata; }
    public String getPredictionParametersJson() { return predictionParametersJson; }
    public void setPredictionParametersJson(String predictionParametersJson) { this.predictionParametersJson = predictionParametersJson; }
    public String getEndpointUrl() { return endpointUrl; }
    public void setEndpointUrl(String endpointUrl) { this.endpointUrl = endpointUrl; }
    public String getSpaceId() { return spaceId; }
    public void setSpaceId(String spaceId) { this.spaceId = spaceId; }
}