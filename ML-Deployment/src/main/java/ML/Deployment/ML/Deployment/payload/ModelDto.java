package ML.Deployment.ML.Deployment.payload;

import java.time.Instant;

// DTO for API responses related to models
public class ModelDto {
    private String id;
    private String name;
    private String description;
    private String originalFilename;
    private long size;
    private Instant uploadTimestamp;
    private String ownerId;
    private String ownerUsername;
    private String deploymentPlatform;
    private String metadata;
    private String predictionParametersJson;
    private String endpointUrl;
    private String spaceId;

    // Getters and Setters for all fields...
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